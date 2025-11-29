package hr.unipu.journals.view.home

import hr.unipu.journals.feature.publication.eic_on_publication.EicOnPublicationRepository
import hr.unipu.journals.feature.invite.InvitationTarget
import hr.unipu.journals.feature.invite.InviteRepository
import hr.unipu.journals.feature.publication.core.PublicationRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class ManageEicOnPublicationPageController(
    private val publicationRepository: PublicationRepository,
    private val eicOnPublicationRepository: EicOnPublicationRepository,
    private val inviteRepository: InviteRepository
) {
    @GetMapping("/publications/{publicationId}/manage-eic-on-publication")
    fun page(@PathVariable publicationId: Int, model: Model): String {
        model["currentPublication"] = publicationRepository.title(publicationId)
        model["eicEmails"] = eicOnPublicationRepository.eicEmailsByPublicationId(publicationId) + inviteRepository.emailsByTarget(InvitationTarget.EIC_ON_PUBLICATION, publicationId)
        return "home/manage/manage-eic-on-publication-page"
    }
}