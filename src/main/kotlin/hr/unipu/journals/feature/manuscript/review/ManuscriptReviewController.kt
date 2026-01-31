package hr.unipu.journals.feature.manuscript.review

import hr.unipu.journals.feature.manuscript.review.round.ManuscriptReviewRoundRepository
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_AUTHOR_ON_MANUSCRIPT_OR_SUPERIOR
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_REVIEWER_ON_MANUSCRIPT_OR_SUPERIOR
import hr.unipu.journals.security.AuthorizationService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/manuscripts/{manuscriptId}/review-rounds")
class ManuscriptReviewController(
    private val authorizationService: AuthorizationService,
    private val manuscriptReviewRepository: ManuscriptReviewRepository,
    private val manuscriptReviewRoundRepositor: ManuscriptReviewRoundRepository,
) {
    @PostMapping("/review")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_REVIEWER_ON_MANUSCRIPT_OR_SUPERIOR)
    fun review(@PathVariable manuscriptId: Int, @RequestParam review: ManuscriptReviewDTO): ResponseEntity<String> {
        val round = manuscriptReviewRoundRepositor.by(manuscriptId = manuscriptId) ?: return ResponseEntity.badRequest().body("manuscript has no ongoing round")
        val rowsAffected = manuscriptReviewRepository.review(
            manuscriptReviewRoundId = round.id,
            reviewerId = authorizationService.account!!.id,
            manuscriptReviewDTO = review
        )
        return if(rowsAffected == 1) ResponseEntity.ok("successfully submitted review")
        else ResponseEntity.internalServerError().body("failed to review")
    }
    @PostMapping("/author-response")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_AUTHOR_ON_MANUSCRIPT_OR_SUPERIOR)
    fun authorResponse(@PathVariable manuscriptId: Int, @RequestParam response: String): ResponseEntity<String> {
        val round = manuscriptReviewRoundRepositor.by(manuscriptId = manuscriptId) ?: return ResponseEntity.badRequest().body("manuscript has no ongoing round")
        val rowsAffected = manuscriptReviewRepository.authorRespond(
            manuscriptReviewRoundId = round.id,
            response = response
        )
        return if(rowsAffected == 1) ResponseEntity.ok("successfully responded")
        else ResponseEntity.internalServerError().body("failed to respond")
    }
}