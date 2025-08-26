package hr.unipu.journals.view.home.publication

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
        model["isAdmin"] = authorizationService.isAdmin()
        model["publications"] = publicationRepository.allPublished().map { publication ->
            val isEicOrSuperior = authorizationService.isEicOnPublicationOrSuperior(publication.id)
            PublicationDTO(
                id = publication.id,
                title = publication.title,
                canHide = isEicOrSuperior,
                canEdit = isEicOrSuperior,
                isEic = isEicOrSuperior
            )
        }.sortedWith(compareByDescending<PublicationDTO> { it.isEic }.thenByDescending { it.id })
        return "home/publication-page"
    }
}