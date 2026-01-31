package hr.unipu.journals.feature.manuscript.review.round

import hr.unipu.journals.EmailService
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.ManuscriptRole
import hr.unipu.journals.feature.manuscript.core.ManuscriptRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptService
import hr.unipu.journals.feature.manuscript.core.ManuscriptState
import hr.unipu.journals.feature.manuscript.review.ManuscriptReviewRepository
import hr.unipu.journals.feature.manuscript.review.Recommendation
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_EDITOR_ON_MANUSCRIPT_OR_SUPERIOR
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/manuscripts/{manuscriptId}/review-rounds")
@PreAuthorize(AUTHORIZATION_SERVICE_IS_EDITOR_ON_MANUSCRIPT_OR_SUPERIOR)
class ManuscriptReviewRoundController(
    private val manuscriptService: ManuscriptService,
    private val manuscriptRepository: ManuscriptRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
    private val manuscriptReviewRoundRepository: ManuscriptReviewRoundRepository,
    private val manuscriptReviewRepository: ManuscriptReviewRepository,
    private val emailService: EmailService
) {
    @Transactional
    @PostMapping("/start")
    fun startRound(@PathVariable manuscriptId: Int): ResponseEntity<String> {
        val response = manuscriptService.updateState(
            manuscriptId = manuscriptId,
            newState = ManuscriptState.AWAITING_REVIEWER_REVIEW
        )
        if(response.statusCode != HttpStatus.OK) return response
        val round = manuscriptReviewRoundRepository.startRound(manuscriptId)
            ?: return ResponseEntity.internalServerError().body("failed to find round")
        accountRoleOnManuscriptRepository.all(manuscriptId = manuscriptId, role = ManuscriptRole.REVIEWER).forEach { reviewer ->
            val rowsAffected = manuscriptReviewRepository.insert(
                reviewerId = reviewer.id,
                manuscriptReviewRoundId = round.id
            )
            if(rowsAffected == 0) return ResponseEntity.internalServerError().body("failed to assign reviewer")
        }
        return ResponseEntity.ok("successfully started round")
    }
    @Transactional
    @PostMapping("/end")
    fun endRound(
        @PathVariable manuscriptId: Int,
        @RequestParam recommendation: Recommendation,
        @RequestParam comment: String,
    ): ResponseEntity<String> {
        val cleanedComment = Jsoup.clean(comment, Safelist.none())
        val rowsModified = manuscriptReviewRoundRepository.endRound(
            manuscriptId = manuscriptId,
            recommendation = recommendation,
            comment = cleanedComment
        )
        if(rowsModified == 0) return ResponseEntity.internalServerError().body("failed to end round")
        if(recommendation in listOf(Recommendation.MINOR, Recommendation.MAJOR)) {
            val manuscript = manuscriptRepository.byId(manuscriptId)
                ?: return ResponseEntity.internalServerError().body("failed to find manuscript")
            val response = manuscriptService.updateState(
                manuscriptId = manuscriptId,
                newState = ManuscriptState.valueOf(recommendation.name)
            )
            if(response.statusCode != HttpStatus.OK) return ResponseEntity.internalServerError().body("failed to end round")
            emailService.sendHtml(
                to = manuscript.correspondingAuthorEmail,
                subject = "Manuscript set to ${recommendation.name.lowercase()}",
                html = "The manuscript ${manuscript.title} has been set to ${recommendation.name.lowercase()}.<br>Editor's comment:<blockquote>$cleanedComment</blockquote>"
            )
        }
        return ResponseEntity.ok("successfully ended round")
    }
}