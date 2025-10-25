package hr.unipu.journals.view.home.manuscript

import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptState
import hr.unipu.journals.security.AuthorizationService
import hr.unipu.journals.view.AccessDeniedException
import hr.unipu.journals.view.InternalServerErrorException
import hr.unipu.journals.view.ResourceNotFoundException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.time.format.DateTimeFormatter

@Controller
class ManuscriptDetailsPageController(
    private val manuscriptRepository: ManuscriptRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
    private val authorizationService: AuthorizationService
) {
    @GetMapping("/manuscript/{manuscriptId}")
    fun page(@PathVariable manuscriptId: Int, model: Model): String {
        val rowsAffected = manuscriptRepository.incrementViews(manuscriptId)
        if(rowsAffected == 0) throw InternalServerErrorException("failed to increment views")
        val manuscript = manuscriptRepository.byId(manuscriptId) ?: throw ResourceNotFoundException("failed to find manuscript $manuscriptId")
        if(manuscript.state !in listOf(ManuscriptState.PUBLISHED, ManuscriptState.ARCHIVED) && authorizationService.isAdmin().not() ) throw AccessDeniedException("not authorized to view manuscript")
        model["id"] = manuscriptId
        model["title"] = manuscript.title
        model["submissionDate"] = manuscript.submissionDate.format(DateTimeFormatter.ofPattern("dd MMM YYYY"))
        model["publicationDate"] = manuscript.publicationDate?.format(DateTimeFormatter.ofPattern("dd MMM YYYY")) ?: "no publication date"
        model["authors"] = accountRoleOnManuscriptRepository.authors(manuscript.id)
        model["abstract"] = manuscript.description
        model["fileUrl"] = manuscript.fileUrl
        model["views"] = manuscript.views
        model["downloads"] = manuscript.downloads
        return "home/manuscript-details-page"
    }
}