package hr.unipu.journals.view.profile
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import hr.unipu.journals.feature.account.AccountRepository
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.manuscript.category.CategoryRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptRepository
import hr.unipu.journals.security.AuthorizationService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class ProfilePageController(
    private val manuscriptRepository: ManuscriptRepository,
    private val authorizationService: AuthorizationService,
    private val accountRepository: AccountRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
    private val categoryRepository: CategoryRepository
) {
    @GetMapping("/profiles/{accountId}")
    fun page(@PathVariable accountId: Int, model: Model): String {
        val account = authorizationService.account
        model["isAccountOwnerOrAdmin"] = authorizationService.isAccountOwner(accountId) || account?.isAdmin ?: false
        model["categories"] = categoryRepository.all()
        accountRepository.byId(accountId)?.let { profile ->
            model["fullName"] = profile.fullName
            model["title"] = profile.title
            if(authorizationService.isAccountOwnerOrAdmin(accountId)) {
                model["author"] = jacksonObjectMapper().writeValueAsString(mapOf(
                    "fullName" to profile.fullName,
                    "title" to profile.title,
                    "email" to profile.email,
                    "affiliation" to profile.affiliation,
                    "jobType" to profile.jobType,
                    "country" to profile.country,
                    "city" to profile.city,
                    "address" to profile.address,
                    "zipCode" to profile.zipCode
                ))
            }
        }
        return "/profile/profile-page"
    }
}