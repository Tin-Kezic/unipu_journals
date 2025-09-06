package hr.unipu.journals.view.profile

import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_ACCOUNT_OWNER_OR_ADMIN
import hr.unipu.journals.security.AuthorizationService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class EditProfilePageController(private val authorizationService: AuthorizationService) {
    @GetMapping("/profile/{accountId}/edit-profile")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ACCOUNT_OWNER_OR_ADMIN)
    fun page(@PathVariable accountId: Int): String {
        val account = authorizationService.account ?: throw IllegalStateException("account with id $accountId not found")
        return """redirect:/profile/$accountId/edit-profile
           &fullName=${account.fullName}
           &title=${account.title}
           &email=${account.email}
           &affiliation=${account.affiliation}
           &jobType=${account.jobType}
           &country=${account.country}
           &city=${account.city}
           &address=${account.address}
           &zipCode=${account.zipCode}
        """
    }
}