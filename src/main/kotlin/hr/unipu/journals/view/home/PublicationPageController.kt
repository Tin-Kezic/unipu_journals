package hr.unipu.journals.view.home

import com.fasterxml.jackson.databind.ObjectMapper
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.manuscript.category.CategoryRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptStateFilter
import hr.unipu.journals.feature.publication.core.Affiliation
import hr.unipu.journals.feature.publication.core.PublicationRepository
import hr.unipu.journals.feature.publication.core.Sorting
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
    private val categoryRepository: CategoryRepository,
    private val objectMapper: ObjectMapper
) {
    @GetMapping("/")
    fun page(
        model: Model,
        @RequestParam publicationId: Int?,
        @RequestParam sectionId: Int?,
        @RequestParam manuscriptStateFilter: ManuscriptStateFilter = ManuscriptStateFilter.PUBLISHED,
        @RequestParam affiliation: Affiliation?,
        @RequestParam category: String?,
        @RequestParam sorting: Sorting?,
        ): String {
        if(publicationId != null && publicationRepository.exists(publicationId).not()) throw ResourceNotFoundException("failed to find publication $publicationId")
        if(sectionId != null && sectionRepository.exists(sectionId).not()) throw ResourceNotFoundException("failed to find section $sectionId")
        val isAdmin = authorizationService.isAdmin
        val publications = publicationRepository.all(
            manuscriptStateFilter = manuscriptStateFilter,
            affiliation = affiliation,
            accountId = authorizationService.account?.id,
            category = category,
            sorting = sorting
        )
        val selectedPublicationId = publicationId ?: publications.first().id
        model["isAdmin"] = isAdmin
        model["isAuthenticated"] = authorizationService.isAuthenticated
        model["categories"] = categoryRepository.all()
        model["publications"] = publications.map { publication -> ContainerDTO(
            id = publication.id,
            title = publication.title,
            canHide = isAdmin,
            canEdit = authorizationService.isEicOnPublicationOrAdmin(publication.id)
        )}
        val sections = sectionRepository.all(
            publicationId = selectedPublicationId,
            manuscriptStateFilter = manuscriptStateFilter,
            affiliation = affiliation,
            accountId = authorizationService.account?.id,
            category = category,
            sorting = sorting
        )
        model["sections"] = sections.map { section -> ContainerDTO(
            id = section.id,
            title = section.title,
            description = section.description,
            canHide = isAdmin,
            canEdit = authorizationService.isSectionEditorOnSectionOrSuperior(selectedPublicationId, section.id)
        )}
        model["isEicOnPublicationOrAdmin"] = authorizationService.isEicOnPublicationOrAdmin(selectedPublicationId)
        model["selectedPublicationId"] = selectedPublicationId
        val selectedSectionId = sectionId ?: if(sections.isNotEmpty()) sections.first().id else {
            model["selectedSectionId"] = -1
            model["description"] = ""
            model["manuscripts"] = listOf<ManuscriptDTO>()
            model["isSectionEditorOnSectionOrSuperior"] = false
            return "home/publication-page"
        }
        model["selectedSectionId"] = selectedSectionId
        model["isSectionEditorOnSectionOrSuperior"] = authorizationService.isSectionEditorOnSectionOrSuperior(selectedPublicationId, selectedSectionId)
        model["description"] = sectionRepository.byId(selectedSectionId).description
        model["manuscripts"] = manuscriptRepository.all(
            sectionId = selectedSectionId,
            manuscriptStateFilter = manuscriptStateFilter,
            affiliation = affiliation,
            accountId = authorizationService.account?.id,
            category = category,
            sorting = sorting
        ).map { manuscript -> ManuscriptDTO(
            id = manuscript.id,
            title = manuscript.title,
            authors = objectMapper.writeValueAsString(accountRoleOnManuscriptRepository.authors(manuscript.id)),
            downloadUrl = manuscript.downloadUrl,
            submissionDate = manuscript.submissionDate.format(DateTimeFormatter.ofPattern("dd MMM YYYY")) ?: "no publication date",
            publicationDate = manuscript.publicationDate?.format(DateTimeFormatter.ofPattern("dd MMM YYYY")) ?: "no publication date",
            description = manuscript.description
        )}
        return "home/publication-page"
    }
}