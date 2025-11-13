package hr.unipu.journals.view.home.manuscript

import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptState
import hr.unipu.journals.feature.publication.core.PublicationRepository
import hr.unipu.journals.feature.section.core.SectionRepository
import hr.unipu.journals.security.AuthorizationService
import hr.unipu.journals.view.ResourceNotFoundException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.time.format.DateTimeFormatter

@Controller
class ManuscriptPageController(
    private val publicationRepository: PublicationRepository,
    private val sectionRepository: SectionRepository,
    private val manuscriptRepository: ManuscriptRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
    private val authorizationService: AuthorizationService
) {
    @GetMapping("/publication/{publicationId}/section/{sectionId}")
    fun page(
        @PathVariable publicationId: Int,
        @PathVariable sectionId: Int,
        model: Model
    ): String {
        if(publicationRepository.exists(publicationId).not()) throw ResourceNotFoundException("failed to find publication $publicationId")
        if(sectionRepository.exists(sectionId).not()) throw ResourceNotFoundException("failed to find section $sectionId")
        model["isAdmin"] = authorizationService.isAdmin
        model["sectionsSidebar"] = sectionRepository.allByPublicationId(publicationId)

        val section = sectionRepository.byId(sectionId)
        model["currentSection"] = section.title
        model["description"] = section.description

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
        model["isEicOrSuperior"] = authorizationService.isEicOnPublicationOrAdmin(publicationId)
        model["isSectionEditorOrSuperior"] = authorizationService.isSectionEditorOnSectionOrSuperior(publicationId, sectionId)
        return "home/manuscript-page"
    }
}