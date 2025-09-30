package hr.unipu.journals.view.archive

import hr.unipu.journals.feature.manuscript.ManuscriptState
import hr.unipu.journals.feature.publication.PublicationRepository
import hr.unipu.journals.security.AuthorizationService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ArchivePublicationPageController(
    private val publicationRepository: PublicationRepository,
    private val authorizationService: AuthorizationService
) {
    @GetMapping("/archive")
    fun page(model: Model): String {
        model["publications"] = publicationRepository.all(ManuscriptState.ARCHIVED).map { publication ->
            ArchiveContainerDTO(
                id = publication.id,
                title = publication.title,
            )
        }
        return "archive/archive-publication-page"
    }
}