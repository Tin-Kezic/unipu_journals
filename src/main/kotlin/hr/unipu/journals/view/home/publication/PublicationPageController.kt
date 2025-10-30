package hr.unipu.journals.view.home.publication

import hr.unipu.journals.feature.publication.core.PublicationRepository
import hr.unipu.journals.feature.publication.core.PublicationType
import hr.unipu.journals.security.AuthorizationService
import hr.unipu.journals.view.home.ContainerDTO
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
        val isAdmin = authorizationService.isAdmin
        model["isAdmin"] = isAdmin
        model["publications"] = publicationRepository.allPublished().map { publication ->
            val isEicOrSuperior = authorizationService.isEicOnPublicationOrSuperior(publication.id)
        model["publications"] = publicationRepository.all(PublicationType.PUBLIC).map { publication ->
            ContainerDTO(
                id = publication.id,
                title = publication.title,
                canHide = isAdmin,
                canEdit = isEicOrSuperior,
                isEditor = isEicOrSuperior
            )
        }.sortedByDescending { it.isEditor }
        return "home/publication/publication-page"
    }
}