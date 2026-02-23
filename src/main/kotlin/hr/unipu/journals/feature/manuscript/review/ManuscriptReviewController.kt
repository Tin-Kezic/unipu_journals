package hr.unipu.journals.feature.manuscript.review

import hr.unipu.journals.EmailService
import hr.unipu.journals.feature.manuscript.core.ManuscriptRepository
import hr.unipu.journals.feature.manuscript.core.ZipService
import hr.unipu.journals.feature.manuscript.review.file.ManuscriptReviewFile
import hr.unipu.journals.feature.manuscript.review.file.ManuscriptReviewFileRepository
import hr.unipu.journals.feature.manuscript.review.file.ManuscriptReviewFileRole
import hr.unipu.journals.feature.manuscript.review.round.ManuscriptReviewRoundRepository
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_AUTHOR_ON_MANUSCRIPT_OR_SUPERIOR
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_REVIEWER_ON_MANUSCRIPT_OR_SUPERIOR
import hr.unipu.journals.security.AuthorizationService
import hr.unipu.journals.security.ClamAv
import hr.unipu.journals.security.ScanResult
import hr.unipu.journals.util.AppProperties
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.UUID

@RestController
@RequestMapping("/api/manuscripts/{manuscriptId}/reviews")
class ManuscriptReviewController(
    private val authorizationService: AuthorizationService,
    private val manuscriptRepository: ManuscriptRepository,
    private val manuscriptReviewRepository: ManuscriptReviewRepository,
    private val manuscriptReviewFileRepository: ManuscriptReviewFileRepository,
    private val manuscriptReviewRoundRepositor: ManuscriptReviewRoundRepository,
    private val clamAv: ClamAv,
    private val zipService: ZipService,
    private val emailService: EmailService,
    private val appProperties: AppProperties,
) {
    @GetMapping
    fun reviews(@PathVariable manuscriptId: Int, @RequestParam isComplete: Boolean): Map<Int, List<Pair<ManuscriptReview, List<ManuscriptReviewFile>>>> {
        return manuscriptReviewRepository.all(manuscriptId = manuscriptId)
            .filter { !isComplete || it.isComplete } // A -> B
            .map { Pair(it, manuscriptReviewFileRepository.all(it.id)) }
            .let { pairs ->
                if(authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscriptId)) return@let pairs
                var rank = 0
                var last: Int? = null
                pairs.map { pair ->
                    if(pair.first.reviewerId != last) {
                        rank++
                        last = pair.first.reviewerId
                    }
                    pair.copy(first = pair.first.copy(reviewerId = rank))
                }
            }
            .groupBy { it.first.reviewerId }
    }
    @PostMapping
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_REVIEWER_ON_MANUSCRIPT_OR_SUPERIOR)
    fun review(
        @PathVariable manuscriptId: Int,
        @RequestPart review: ManuscriptReviewDTO,
        @RequestPart files: List<MultipartFile>,
    ): ResponseEntity<String> {
        files.forEach { file ->
            if(file.originalFilename == null)
                return ResponseEntity.badRequest().body("submitted unnamed files")
        }
        val tempFiles = files.map { file ->
            val cleanFileName = Jsoup.clean(file.originalFilename!!, Safelist.none())
            cleanFileName to File.createTempFile(
                UUID.randomUUID().toString(),
                "." + cleanFileName.substringAfter(".")
            ).apply { deleteOnExit() }
        }
        files.zip(tempFiles).forEach { (file, temp) -> file.transferTo(temp.second) }
        try {
            tempFiles.forEach { (name, file) ->
                val extension = file.name.substringAfterLast('.', "").lowercase()
                if(extension in clamAv.forbiddenExtensions)
                    return ResponseEntity.badRequest().body("files of type .$extension are not allowed.")
                if(extension == "zip" && zipService.isEncrypted(file))
                    return ResponseEntity.badRequest().body("submitted zip files are encrypted, corrupted or malformed")
                if(clamAv.scanMultipartFile(file) == ScanResult.FOUND)
                    return ResponseEntity.badRequest().body("submitted files contain malware")
            }
            val round = manuscriptReviewRoundRepositor.latest(manuscriptId = manuscriptId) ?: return ResponseEntity.badRequest().body("manuscript has no ongoing round")
            val reviewerId = authorizationService.account!!.id
            if(manuscriptReviewRepository.isComplete(
                manuscriptReviewRoundId = round.id,
                reviewerId = reviewerId
            )) return ResponseEntity.badRequest().body("review has already been submitted")

            val cleanReviewerComment = Jsoup.clean(review.reviewerComment ?: "", Safelist.none())
            val review = manuscriptReviewRepository.review(
                manuscriptReviewRoundId = round.id,
                reviewerId = reviewerId,
                manuscriptReviewDTO = review.copy(reviewerComment = cleanReviewerComment)
            )
            tempFiles.forEach { (name, file) ->
                val path = "${appProperties.fileStoragePath}/${file.name}"
                file.copyTo(File(path), true)
                manuscriptReviewFileRepository.insert(
                    name = name,
                    path = path,
                    reviewId = review.id,
                    fileRole = ManuscriptReviewFileRole.REVIEW
                )
            }
            val manuscriptReview = manuscriptReviewRepository.byId(review.id)
                ?: return ResponseEntity.internalServerError().body("failed to find reviewer")
            val manuscriptReviewRound = manuscriptReviewRoundRepositor.byId(manuscriptReview.manuscriptReviewRoundId)
                ?: return ResponseEntity.internalServerError().body("failed to find reviewer")
            val manuscript = manuscriptRepository.byId(manuscriptReviewRound.manuscriptId)
                ?: return ResponseEntity.internalServerError().body("failed to find manuscript")

            emailService.sendHtml(
                to = manuscript.correspondingAuthorEmail,
                subject = "new review submission",
                html = """
                    a new review has been submitted to manuscript: "${manuscript.title}".<br>
                    reviewer's comment:<blockquote>${cleanReviewerComment}</blockquote>
                    <a href="${appProperties.baseUrl}/manuscripts/${manuscript.id}/review-history?id=${review.id}">see full review</a>
                """.trimIndent()
            )
            return ResponseEntity.ok("manuscript successfully added")
        } finally { tempFiles.forEach { (name, file) -> file.delete() } }
    }
    @PostMapping("/author-response")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_AUTHOR_ON_MANUSCRIPT_OR_SUPERIOR)
    fun authorResponse(@PathVariable manuscriptId: Int, @RequestParam response: String): ResponseEntity<String> {
        val round = manuscriptReviewRoundRepositor.latest(manuscriptId = manuscriptId)
        if(round == null || round.isComplete) return ResponseEntity.badRequest().body("manuscript has no ongoing round")
        val rowsAffected = manuscriptReviewRepository.authorRespond(
            manuscriptReviewRoundId = round.id,
            response = response
        )
        return if(rowsAffected == 1) ResponseEntity.ok("successfully responded")
        else ResponseEntity.internalServerError().body("failed to respond")
    }
}