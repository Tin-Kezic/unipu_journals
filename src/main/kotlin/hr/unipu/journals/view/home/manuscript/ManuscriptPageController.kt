package hr.unipu.journals.view.home.manuscript

import hr.unipu.journals.feature.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.manuscript.ManuscriptRepository
import hr.unipu.journals.feature.manuscript.ManuscriptState
import hr.unipu.journals.feature.publication.PublicationRepository
import hr.unipu.journals.feature.section.SectionRepository
import hr.unipu.journals.security.AuthorizationService
import hr.unipu.journals.view.ResourceNotFoundException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.time.format.DateTimeFormatter

@Controller
@RequestMapping("/publication")
class ManuscriptPageController(
    private val publicationRepository: PublicationRepository,
    private val sectionRepository: SectionRepository,
    private val manuscriptRepository: ManuscriptRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
    private val authorizationService: AuthorizationService
) {
    @GetMapping("/{publicationId}/section/{sectionId}")
    fun page(
        @PathVariable publicationId: Int,
        @PathVariable sectionId: Int,
        model: Model
    ): String {
        if(publicationRepository.exists(publicationId).not()) throw ResourceNotFoundException("failed to find publication with id $publicationId")
        if(sectionRepository.exists(sectionId).not()) throw ResourceNotFoundException("failed to find section with id: $sectionId")
        model["isAdmin"] = authorizationService.isAdmin()
        model["sectionsSidebar"] = sectionRepository.allByPublicationId(publicationId)
        model["description"] = sectionRepository.description(sectionId)
        model["publicationId"] = publicationId
        model["sectionId"] = sectionId
        model["manuscripts"] = manuscriptRepository.allBySectionId(sectionId = sectionId, manuscriptState = ManuscriptState.PUBLISHED).map { manuscript ->
            ManuscriptDTO(
                id = manuscript.id,
                title = manuscript.title,
                authors = accountRoleOnManuscriptRepository.authors(manuscript.id),
                fileUrl = manuscript.fileUrl,
                publicationDate = manuscript.publicationDate?.format(DateTimeFormatter.ofPattern("dd MMM YYYY")) ?: "no publication date",
                description = manuscript.description
            )
        }
        model["currentSection"] = sectionRepository.title(sectionId)
        model["isEicOrSuperior"] = authorizationService.isEicOnPublicationOrSuperior(publicationId)
        model["isSectionEditorOrSuperior"] = authorizationService.isSectionEditorOnSectionOrSuperior(publicationId, sectionId)
        return "home/manuscript-page"
    }
}