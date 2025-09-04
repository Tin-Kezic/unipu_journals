package hr.unipu.journals.view.hidden

import hr.unipu.journals.feature.publication.PublicationRepository
import hr.unipu.journals.security.AuthorizationService
import hr.unipu.journals.view.home.ContainerDTO
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HiddenPublicationPageController(
    private val publicationRepository: PublicationRepository,
    private val authorizationService: AuthorizationService
) {
    @GetMapping("/hidden")
    fun page(model: Model): String {
        model["publications"] = publicationRepository.allHidden().map { publication ->
            val isEicOrSuperior = authorizationService.isEicOnPublicationOrSuperior(publication.id)
            ContainerDTO(
                id = publication.id,
                title = publication.title,
                canHide = isEicOrSuperior,
                canEdit = isEicOrSuperior,
                isEditor = isEicOrSuperior
            )
        }.sortedByDescending { it.isEditor }
        return "hidden/hidden-publication-page"
    }
}
