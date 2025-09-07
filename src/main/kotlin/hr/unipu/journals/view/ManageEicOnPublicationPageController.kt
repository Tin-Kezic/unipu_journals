package hr.unipu.journals.view

import hr.unipu.journals.feature.eic_on_publication.EicOnPublicationRepository
import hr.unipu.journals.feature.publication.PublicationRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Controller
class ManageEicOnPublicationPageController(
    private val publicationRepository: PublicationRepository,
    private val eicOnPublicationRepository: EicOnPublicationRepository
) {
    @GetMapping("/publication/{publicationId}/manage-eic-on-publication")
    fun page(@PathVariable publicationId: Int, model: Model): String {
        model["currentPublication"] = publicationRepository.title(publicationId)
        model["eicEmails"] = eicOnPublicationRepository.eicEmailsByPublicationId(publicationId)
        return "manage/manage-eic-on-publication-page"
    }
}
