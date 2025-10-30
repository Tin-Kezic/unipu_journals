package hr.unipu.journals.view.archive

import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptState
import hr.unipu.journals.feature.section.core.SectionRepository
import hr.unipu.journals.security.AuthorizationService
import hr.unipu.journals.view.home.manuscript.ManuscriptDTO
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.time.format.DateTimeFormatter

@Controller
class ArchiveManuscriptPageController(
    private val sectionRepository: SectionRepository,
    private val manuscriptRepository: ManuscriptRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
    private val authorizationService: AuthorizationService
) {
    @GetMapping("archive/publication/{publicationId}/section/{sectionId}")
    fun page(
        @PathVariable publicationId: Int,
        @PathVariable sectionId: Int,
        model: Model
    ): String {
        model["isAdmin"] = authorizationService.isAdmin
        model["sectionsSidebar"] = sectionRepository.allByPublicationId(publicationId, ManuscriptState.ARCHIVED)
        model["manuscripts"] = manuscriptRepository.allBySectionId(sectionId = sectionId, manuscriptState = ManuscriptState.ARCHIVED).map { manuscript ->
            ManuscriptDTO(
                id = manuscript.id,
                title = manuscript.title,
                authors = accountRoleOnManuscriptRepository.authors(manuscript.id),
                fileUrl = manuscript.fileUrl,
                publicationDate = manuscript.publicationDate?.format(DateTimeFormatter.ofPattern("dd MMM YYYY"))
                    ?: "no publication date",
                description = manuscript.description
            )
        }
        model["currentSection"] = sectionRepository.byId(sectionId).title
        return "archive/archive-manuscript-page"
    }
}
