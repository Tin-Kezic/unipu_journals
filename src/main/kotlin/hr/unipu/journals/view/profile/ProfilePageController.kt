package hr.unipu.journals.view.profile

import hr.unipu.journals.feature.account.AccountRepository
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptState
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
    private val accountRepository: AccountRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository
) {
    @GetMapping("/profiles/{accountId}")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ACCOUNT_OWNER_OR_ADMIN)
    fun page(
        @PathVariable accountId: Int,
        //@RequestParam fragment,
        model: Model
    ): String {
        val account = authorizationService.account ?: throw IllegalStateException("account is null")
        accountRepository.byId(account.id)?.let { profile ->
            model["fullName"] = profile.fullName
            model["title"] = profile.title
            model["email"] = profile.email
            model["affiliation"] = profile.affiliation
            model["jobType"] = profile.jobType
            model["country"] = profile.country
            model["city"] = profile.city
            model["address"] = profile.address
            model["zipCode"] = profile.zipCode
        } ?: throw IllegalStateException("failed to find account ${account.id}")

        model["isAdmin"] = account.isAdmin
        val manuscripts = manuscriptRepository.all(accountId = accountId)
        model["minor-major"] = manuscripts
            .filter { manuscript -> manuscript.state == ManuscriptState.MINOR || manuscript.state == ManuscriptState.MAJOR }
            .map { manuscript ->
                ProfileManuscriptDTO(
                    id = manuscript.id,
                    title = manuscript.title,
                    authors = accountRoleOnManuscriptRepository.authors(manuscript.id),
                    publicationDate = manuscript.publicationDate?.format(DateTimeFormatter.ofPattern("dd MMM YYYY")) ?: "no publication date",
                    description = manuscript.description
                )
            }
        model["manuscripts"] = manuscripts.map { manuscript ->
            ProfileManuscriptDTO(
                id = manuscript.id,
                title = manuscript.title,
                authors = accountRoleOnManuscriptRepository.authors(manuscript.id),
                publicationDate = manuscript.publicationDate?.format(DateTimeFormatter.ofPattern("dd MMM YYYY")) ?: "no publication date",
                description = manuscript.description
            )
        }
        return "/profile/profile-page"
    }
}