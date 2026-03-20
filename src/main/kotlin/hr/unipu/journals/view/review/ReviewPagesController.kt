package hr.unipu.journals.view.review

import hr.unipu.journals.feature.account.AccountRepository
import hr.unipu.journals.feature.invite.InvitationTarget
import hr.unipu.journals.feature.invite.InviteRepository
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.ManuscriptRole
import hr.unipu.journals.feature.manuscript.core.ManuscriptRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptService
import hr.unipu.journals.feature.manuscript.core.ManuscriptState
import hr.unipu.journals.feature.manuscript.review.ManuscriptReviewRepository
import hr.unipu.journals.feature.manuscript.review.file.ManuscriptReviewFileRepository
import hr.unipu.journals.feature.manuscript.review.file.ManuscriptReviewFileRole
import hr.unipu.journals.feature.manuscript.review.round.ManuscriptReviewRoundRepository
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_EDITOR_ON_MANUSCRIPT_OR_SUPERIOR
import hr.unipu.journals.security.AuthorizationService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class ReviewPagesController(
    private val authorizationService: AuthorizationService,
    private val accountRepository: AccountRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
    private val inviteRepository: InviteRepository,
    private val manuscriptReviewRepository: ManuscriptReviewRepository,
    private val manuscriptReviewFileRepository: ManuscriptReviewFileRepository,
    private val manuscriptReviewRoundRepository: ManuscriptReviewRoundRepository,
    private val manuscriptRepository: ManuscriptRepository,
    private val manuscriptService: ManuscriptService
) {
    @GetMapping("/manuscripts/{manuscriptId}/review")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_EDITOR_ON_MANUSCRIPT_OR_SUPERIOR)
    fun page(@PathVariable manuscriptId: Int, model: Model): String {
        val manuscript = manuscriptRepository.byId(manuscriptId) ?: throw IllegalArgumentException("failed to find manuscript $manuscriptId")
        model["manuscript"] = manuscriptService.toManuscriptDto(manuscript)
        return when(manuscript.state) {
            ManuscriptState.AWAITING_EIC_REVIEW -> {
                require(authorizationService.isEicOnManuscript(manuscriptId))
                model["type"] = "EIC"
                "/review/initial"
            }
            ManuscriptState.AWAITING_EDITOR_REVIEW -> {
                require(authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscriptId))
                model["type"] = "EDITOR"
                "/review/initial"
            }
            ManuscriptState.AWAITING_ROUND_INITIALIZATION -> {
                require(authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscriptId))
                model["type"] = if(authorizationService.isEicOnManuscript(manuscriptId)) "EIC" else "EDITOR"
                val registeredReviewers = accountRoleOnManuscriptRepository.all(role = ManuscriptRole.REVIEWER, manuscriptId = manuscriptId)
                        .map { accountRepository.byId(it.accountId)!! }
                        .map { it.email }
                val unregisteredReviewers = inviteRepository.all(target = InvitationTarget.REVIEWER, targetId = manuscriptId).map { it.email }
                model["reviewers"] = registeredReviewers + unregisteredReviewers
                val round = manuscriptReviewRoundRepository.latest(manuscriptId)
                model["editorRecommendation"] = round?.editorRecommendation ?: ""
                model["editorComment"] = round?.editorComment ?: ""
                "/review/round-initialization-page"
            }
            ManuscriptState.AWAITING_REVIEWER_REVIEW -> {
                require(authorizationService.isReviewerOnManuscriptOrAffiliatedSuperior(manuscriptId))
                val type = if(authorizationService.isEicOnManuscript(manuscriptId)) "EIC" else if(authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscriptId)) "EDITOR" else null
                model["type"] = type
                model["reviewerWithRounds"] = manuscriptReviewRepository.all(manuscriptId = manuscriptId)
                    .groupBy { it.reviewerId }
                    .toSortedMap()
                    .map { (reviewerId, reviews) -> buildMap {
                        put("reviewerId", reviewerId)
                        put("review", reviews.mapIndexed { index, review -> buildMap {
                            val files = manuscriptReviewFileRepository.all(review.id)
                            put("reviewerId", if(type != null) review.reviewerId else null)
                            put("reviewerComment", review.reviewerComment)
                            put("reviewerCommentFiles", files.filter { it.fileRole == ManuscriptReviewFileRole.REVIEW })
                            put("authorResponse", review.authorResponse)
                            put("authorResponseFiles", files.filter { it.fileRole == ManuscriptReviewFileRole.AUTHOR_RESPONSE })
                            put("round", index + 1)
                            put("roundId", review.manuscriptReviewRoundId)
                        }})
                    }}
                "/review/review-page"
            }
            else -> throw IllegalArgumentException("manuscript is currently not under review")
        }
    }
}