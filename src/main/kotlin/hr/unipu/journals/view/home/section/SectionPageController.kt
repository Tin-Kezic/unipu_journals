package hr.unipu.journals.view.home.section

import hr.unipu.journals.feature.publication.core.PublicationRepository
import hr.unipu.journals.feature.publication.core.PublicationType
import hr.unipu.journals.feature.section.core.SectionRepository
import hr.unipu.journals.security.AuthorizationService
import hr.unipu.journals.view.ResourceNotFoundException
import hr.unipu.journals.view.home.ContainerDTO
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class SectionPageController(
    private val publicationRepository: PublicationRepository,
    private val sectionRepository: SectionRepository,
    private val authorizationService: AuthorizationService
) {
    @GetMapping("/publication/{publicationId}")
    fun page(@PathVariable publicationId: Int, model: Model): String {
        if(publicationRepository.exists(publicationId).not()) throw ResourceNotFoundException("failed to find publication $publicationId")
        model["publicationsSidebar"] = publicationRepository.allPublished()
        model["isAdmin"] = authorizationService.isAdmin()
        val isAdmin = authorizationService.isAdmin
        model["publicationsSidebar"] = publicationRepository.all(PublicationType.PUBLIC)
        model["isAdmin"] = isAdmin
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
                canHide = isAdmin,
            )
        }
        return "home/section/section-page"
    }
}