package hr.unipu.journals.view

import hr.unipu.journals.security.AuthorizationService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HeaderController(private val authorizationService: AuthorizationService) {

    @GetMapping("/header/profile")
    fun user(model: Model): String {
        val account = authorizationService.account ?: return "util/partial/login-button"
        model["id"] = account.id
        model["full-name"] = account.fullName
        model["title"] = account.title
        return "util/partial/profile-button"
    }
}