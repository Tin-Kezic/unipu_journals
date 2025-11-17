package hr.unipu.journals.view.home

import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.manuscript.category.CategoryRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptState
import hr.unipu.journals.feature.manuscript.core.ManuscriptStateFilter
import hr.unipu.journals.feature.publication.core.PublicationRepository
import hr.unipu.journals.feature.section.core.SectionRepository
import hr.unipu.journals.security.AuthorizationService
import hr.unipu.journals.view.ResourceNotFoundException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.format.DateTimeFormatter

@Controller
class PublicationPageController(
    private val publicationRepository: PublicationRepository,
    private val sectionRepository: SectionRepository,
    private val manuscriptRepository: ManuscriptRepository,
    private val authorizationService: AuthorizationService,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
    private val categoryRepository: CategoryRepository
) {
    @GetMapping("/")
    fun page(
        model: Model,
        @RequestParam publicationId: Int?,
        @RequestParam sectionId: Int?
        ): String {
        if(publicationId != null && publicationRepository.exists(publicationId).not()) throw ResourceNotFoundException("failed to find publication $publicationId")
        if(sectionId != null && sectionRepository.exists(sectionId).not()) throw ResourceNotFoundException("failed to find section $sectionId")
        val isAdmin = authorizationService.isAdmin
        val publications = publicationRepository.all(ManuscriptStateFilter.PUBLISHED)
        val selectedPublicationId = publicationId ?: publications.first().id
        model["isAdmin"] = isAdmin
        model["isAuthenticated"] = authorizationService.isAuthenticated

        // todo. fix
        model["isEicOnPublicationOrAdmin"] = authorizationService.isEicOnPublicationOrAdmin(selectedPublicationId)
        model["isSectionEditorOrSuperior"] = authorizationService.isSectionEditorOnSectionOrSuperior(publicationId ?: 0, sectionId ?: 0)

        model["categories"] = categoryRepository.all()
        model["publications"] = publications.map { publication ->
            ContainerDTO(
                id = publication.id,
                title = publication.title,
                canHide = isAdmin,
                canEdit = authorizationService.isEicOnPublicationOrAdmin(publication.id)
            )
        }
        val sections = sectionRepository.allByPublicationId(selectedPublicationId)
        model["sections"] = sections.map { section ->
            ContainerDTO(
                id = section.id,
                title = section.title,
                canHide = isAdmin,
                canEdit = authorizationService.isSectionEditorOnSectionOrSuperior(selectedPublicationId, section.id)
            )
        }
        val selectedSectionId = sectionId ?: sections.first().id
        model["description"] = sectionRepository.byId(selectedSectionId).description
        model["manuscripts"] = manuscriptRepository.allBySectionId(sectionId = selectedSectionId, manuscriptState = ManuscriptState.PUBLISHED).map { manuscript ->
            ManuscriptDTO(
                id = manuscript.id,
                title = manuscript.title,
                authors = accountRoleOnManuscriptRepository.authors(manuscript.id),
                fileUrl = manuscript.fileUrl,
                publicationDate = manuscript.publicationDate?.format(DateTimeFormatter.ofPattern("dd MMM YYYY")) ?: "no publication date",
                description = manuscript.description
            )
        }
        return "home/publication-page"
    }
}