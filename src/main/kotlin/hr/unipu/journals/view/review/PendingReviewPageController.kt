package hr.unipu.journals.view.review

import hr.unipu.journals.feature.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.invite.InviteRepository
import hr.unipu.journals.feature.manuscript.Manuscript
import hr.unipu.journals.feature.manuscript.ManuscriptRepository
import hr.unipu.journals.feature.publication.PublicationRepository
import hr.unipu.journals.security.AuthorizationService
import hr.unipu.journals.view.home.manuscript.ManuscriptDTO
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.format.DateTimeFormatter

@Controller
class PendingReviewPageController(
    private val publicationRepository: PublicationRepository,
    private val manuscriptRepository: ManuscriptRepository,
    private val inviteRepository: InviteRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
    private val authorizationService: AuthorizationService
) {
    fun List<Manuscript>.toManuscriptDTO() = this.map { manuscript ->
        ManuscriptDTO(
            id = manuscript.id,
            title = manuscript.title,
            authors = accountRoleOnManuscriptRepository.authors(manuscript.id),
            fileUrl = manuscript.fileUrl,
            publicationDate = manuscript.publicationDate?.format(DateTimeFormatter.ofPattern("dd MMM YYYY")) ?: "no publication date",
            description = manuscript.description
        )
    }
    @GetMapping("/review")
    fun all(@RequestParam publicationId: Int?, model: Model): String {
        authorizationService.account?.let { account ->
            model["publicationsSidebar"] =
                publicationRepository.allWhichContainManuscriptsUnderReviewWithAffiliation(account.id) +
                inviteRepository.allPublicationsWhichContainManuscriptsUnderReviewWithAffiliation(account.email)
            model["invited"] = inviteRepository.affiliatedManuscripts(account.email, publicationId).toManuscriptDTO()
            model["pending"] = manuscriptRepository.pending(account.id, publicationId).toManuscriptDTO()
        } ?: throw IllegalStateException("account is null")
        model["publicationId"] = publicationId ?: 0
        return "review/pending-review-page"
    }
    @GetMapping("/navigate-to-manuscript-page")
    fun navigateToManuscriptPage(@RequestParam manuscriptId: Int): String {
        val (publicationId, sectionId) = manuscriptRepository.publicationIdAndSectionId(manuscriptId)
        return "redirect:/publication/$publicationId/section/$sectionId/manuscript/$manuscriptId"
    }
}