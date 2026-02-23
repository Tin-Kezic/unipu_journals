package hr.unipu.journals.feature.manuscript.review.round

import hr.unipu.journals.EmailService
import hr.unipu.journals.feature.account.AccountRepository
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.ManuscriptRole
import hr.unipu.journals.feature.manuscript.core.ManuscriptRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptService
import hr.unipu.journals.feature.manuscript.core.ManuscriptState
import hr.unipu.journals.feature.manuscript.review.ManuscriptReviewRepository
import hr.unipu.journals.feature.manuscript.review.Recommendation
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_EDITOR_ON_MANUSCRIPT_OR_SUPERIOR
import hr.unipu.journals.security.AuthorizationService
import hr.unipu.journals.util.AppProperties
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/manuscripts/{manuscriptId}/review-rounds")
@PreAuthorize(AUTHORIZATION_SERVICE_IS_EDITOR_ON_MANUSCRIPT_OR_SUPERIOR)
class ManuscriptReviewRoundController(
    private val accountRepository: AccountRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
    private val manuscriptService: ManuscriptService,
    private val manuscriptRepository: ManuscriptRepository,
    private val manuscriptReviewRoundRepository: ManuscriptReviewRoundRepository,
    private val manuscriptReviewRepository: ManuscriptReviewRepository,
    private val authorizationService: AuthorizationService,
    private val emailService: EmailService,
    private val appProperties: AppProperties
) {
    @PostMapping("/start")
    fun startRound(@PathVariable manuscriptId: Int): ResponseEntity<String> {
        val response = manuscriptService.updateState(
            manuscriptId = manuscriptId,
            newState = ManuscriptState.AWAITING_REVIEWER_REVIEW
        )
        if(response.statusCode != HttpStatus.OK) return response
        val round = manuscriptReviewRoundRepository.startRound(manuscriptId)
            ?: return ResponseEntity.internalServerError().body("failed to find round")
        val eicId = accountRoleOnManuscriptRepository.eicOnManuscript(manuscriptId).accountId
        val editorId = accountRoleOnManuscriptRepository.editorOnManuscript(manuscriptId).accountId
        val reviewerIds = accountRoleOnManuscriptRepository.all(manuscriptId = manuscriptId, role = ManuscriptRole.REVIEWER).map { it.accountId }
        (setOf(eicId, editorId) + reviewerIds).forEach { reviewerId ->
            val rowsAffected = manuscriptReviewRepository.insert(
                reviewerId = reviewerId,
                manuscriptReviewRoundId = round.id
            )
            if(rowsAffected == 0) return ResponseEntity.internalServerError().body("failed to assign reviewer")
        }
        return ResponseEntity.ok("successfully started round")
    }
    @PostMapping("/end")
    fun endRound(
        @PathVariable manuscriptId: Int,
        @RequestParam recommendation: Recommendation,
        @RequestParam editorComment: String,
    ): ResponseEntity<String> {
        val cleanedComment = Jsoup.clean(editorComment, Safelist.none())
        val rowsModified = manuscriptReviewRoundRepository.endRound(
            manuscriptId = manuscriptId,
            recommendation = recommendation,
            comment = cleanedComment
        )
        if(rowsModified == 0) return ResponseEntity.internalServerError().body("failed to end round")
        val manuscript = manuscriptRepository.byId(manuscriptId)
            ?: return ResponseEntity.internalServerError().body("failed to find manuscript")
        val eicId = accountRoleOnManuscriptRepository.eicOnManuscript(manuscriptId).accountId
        val isEic = authorizationService.account?.id == eicId
        val newState = when {
            recommendation in setOf(Recommendation.MINOR, Recommendation.MAJOR) -> ManuscriptState.valueOf(recommendation.name)
            isEic && recommendation == Recommendation.ACCEPT -> ManuscriptState.PUBLISHED
            isEic && recommendation == Recommendation.REJECT -> ManuscriptState.REJECTED
            else -> ManuscriptState.AWAITING_ROUND_INITIALIZATION
        }
        val response = manuscriptService.updateState(
            manuscriptId = manuscriptId,
            newState = newState
        )
        if(response.statusCode != HttpStatus.OK) return response
        emailService.sendHtml(
            to = manuscript.correspondingAuthorEmail,
            subject = "Manuscript set to ${newState.name.lowercase()}",
            html = """
                The manuscript ${manuscript.title} has been set to ${newState.name.lowercase()}.<br>
                Editor's comment:<blockquote>$cleanedComment</blockquote>
                <a href="${appProperties.baseUrl}/manuscripts/${manuscript.id}/review-history">see review history</a>
            """.trimIndent()
        )
        if(isEic.not() && recommendation in setOf(Recommendation.ACCEPT, Recommendation.REJECT)) {
            accountRepository.byId(eicId)?.let {
                emailService.sendHtml(
                    to = it.email,
                    subject = "Manuscript round ended",
                    html = """
                        round on manuscript ${manuscript.title} has ended.<br>
                        Editor's recommendation: ${recommendation}<br>Editor's comment:<blockquote>$cleanedComment</blockquote>
                        <a href="${appProperties.baseUrl}/manuscripts/${manuscript.id}/review-history">see review history</a>
                    """.trimIndent()
                )
            }
        }
        return ResponseEntity.ok("successfully ended round")
    }
}