package hr.unipu.journals.view.home.section

import hr.unipu.journals.feature.publication.PublicationRepository
import hr.unipu.journals.feature.section.SectionRepository
import hr.unipu.journals.security.AuthorizationService
import hr.unipu.journals.view.home.ContainerDTO
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/publication")
class SectionPageController(
    private val publicationRepository: PublicationRepository,
    private val sectionRepository: SectionRepository,
    private val authorizationService: AuthorizationService
) {
    @GetMapping("/{publicationId}")
    fun page(@PathVariable publicationId: Int, model: Model): String {
        model["publicationsSidebar"] = publicationRepository.all()
        model["isAdmin"] = authorizationService.isAdmin()
        model["isEicOrSuperior"] = authorizationService.isEicOnPublicationOrSuperior(publicationId)
        model["currentPublication"] = publicationRepository.title(publicationId)
        model["sections"] = sectionRepository.allByPublicationId(publicationId).map { section ->
            val isSectionEditorOrSuperior = authorizationService.isSectionEditorOnSectionOrSuperior(publicationId, section.id)
            ContainerDTO(
                id = section.id,
                title = section.title,
                canHide = authorizationService.isAdmin(),
                canEdit = isSectionEditorOrSuperior,
                isEditor = isSectionEditorOrSuperior
            )
        }.sortedByDescending { it.isEditor }
        return "home/section-page"
    }
}