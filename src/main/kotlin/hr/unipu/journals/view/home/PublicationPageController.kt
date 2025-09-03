package hr.unipu.journals.view.home

import hr.unipu.journals.feature.publication.PublicationRepository
import hr.unipu.journals.security.AuthorizationService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping

@Controller
class PublicationPageController(
    private val publicationRepository: PublicationRepository,
    private val authorizationService: AuthorizationService
) {
    @GetMapping("/")
    fun page(model: Model): String {
        val isAdmin = authorizationService.isAdmin()
        model["isAdmin"] = isAdmin
        model["publications"] = publicationRepository.allPublished().map { publication ->
            val isEicOrSuperior = authorizationService.isEicOnPublicationOrSuperior(publication.id)
            ContainerDTO(
                id = publication.id,
                title = publication.title,
                canHide = isAdmin,
                canEdit = isEicOrSuperior,
                isEic = isEicOrSuperior
            )
        }.sortedByDescending { it.isEic }
        return "home/publication-page"
    }
}