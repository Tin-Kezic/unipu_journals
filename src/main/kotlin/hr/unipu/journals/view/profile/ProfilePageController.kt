package hr.unipu.journals.view.profile

import hr.unipu.journals.feature.manuscript.ManuscriptRepository
import hr.unipu.journals.feature.manuscript.ManuscriptState
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_ACCOUNT_OWNER_OR_ADMIN
import hr.unipu.journals.security.AuthorizationService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.time.format.DateTimeFormatter

@Controller
class ProfilePageController(
    private val manuscriptRepository: ManuscriptRepository,
    private val authorizationService: AuthorizationService,
) {
    @GetMapping("/profile/{accountId}")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ACCOUNT_OWNER_OR_ADMIN)
    fun page(@PathVariable accountId: Int, model: Model): String {
        model["isAdmin"] = authorizationService.isAdmin()
        val manuscripts = manuscriptRepository.allByAuthor(accountId)
        model["minor-major"] = manuscripts
            .filter { manuscript -> manuscript.state == ManuscriptState.MINOR_FIXES || manuscript.state == ManuscriptState.MAJOR_FIXES }
            .map { manuscript ->
                ProfileManuscriptDTO(
                    id = manuscript.id,
                    title = manuscript.title,
                    publicationDate = manuscript.publicationDate?.format(DateTimeFormatter.ofPattern("dd MMM YYYY")) ?: "no publication date",
                    description = manuscript.description
                )
            }
        model["manuscripts"] = manuscripts.map { manuscript ->
            ProfileManuscriptDTO(
                id = manuscript.id,
                title = manuscript.title,
                publicationDate = manuscript.publicationDate?.format(DateTimeFormatter.ofPattern("dd MMM YYYY")) ?: "no publication date",
                description = manuscript.description
            )
        }
        return "/profile/profile-page"
    }
}