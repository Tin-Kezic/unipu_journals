package hr.unipu.journals.view.profile

import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_ACCOUNT_OWNER_OR_ADMIN
import hr.unipu.journals.security.AuthorizationService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class EditProfilePageController(private val authorizationService: AuthorizationService) {
    @GetMapping("/profiles/{accountId}/edit")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ACCOUNT_OWNER_OR_ADMIN)
    fun page(@PathVariable accountId: Int, model: Model): String {
        model["account"] = authorizationService.account ?: throw IllegalStateException("failed to find account $accountId")
        return "profile/edit-profile-page"
    }
}