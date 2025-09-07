package hr.unipu.journals.view.home

import hr.unipu.journals.feature.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.manuscript.ManuscriptRepository
import hr.unipu.journals.feature.section.SectionRepository
import hr.unipu.journals.security.AuthorizationService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.time.format.DateTimeFormatter

@Controller
@RequestMapping("/publication")
class ManuscriptDetailsPageController(
    private val sectionRepository: SectionRepository,
    private val manuscriptRepository: ManuscriptRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
    private val authorizationService: AuthorizationService
) {
    @GetMapping("/{publicationId}/section/{sectionId}/manuscript/{manuscriptId}")
    fun page(
        @PathVariable publicationId: Int,
        @PathVariable sectionId: Int,
        @PathVariable manuscriptId: Int,
        model: Model
    ): String {
        val manuscript = manuscriptRepository.byId(manuscriptId)
        model["id"] = manuscriptId
        model["title"] = manuscript.title
        model["submissionDate"] = manuscript.submissionDate.format(DateTimeFormatter.ofPattern("dd MMM YYYY"))
        model["publicationDate"] = manuscript.publicationDate?.format(DateTimeFormatter.ofPattern("dd MMM YYYY")) ?: "no publication date"
        model["authors"] = accountRoleOnManuscriptRepository.authors(manuscript.id)
        model["abstract"] = manuscript.description
        model["fileUrl"] = manuscript.fileUrl
        model["views"] = manuscript.views
        model["isSectionEditorOnSection"] = authorizationService.isSectionEditorOnSectionOrSuperior(publicationId, sectionId)
        return "home/manuscript-page"
    }
}