package hr.unipu.journals.view.archive

import hr.unipu.journals.feature.publication.PublicationRepository
import hr.unipu.journals.security.AuthorizationService
import hr.unipu.journals.view.home.ContainerDTO
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
        model["publications"] = publicationRepository.allArchived().map { publication ->
            val isEicOrSuperior = authorizationService.isEicOnPublicationOrSuperior(publication.id)
            ContainerDTO(
                id = publication.id,
                title = publication.title,
                canHide = isEicOrSuperior,
                canEdit = isEicOrSuperior,
                isEic = isEicOrSuperior
            )
        }.sortedByDescending { it.isEic }
        return "archive/archive-publication-page"
    }
}