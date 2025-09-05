package hr.unipu.journals.view.profile

import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_ACCOUNT_OWNER_OR_SUPERIOR
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class ProfilePageController() {
    @GetMapping("/profile/{accountId}")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ACCOUNT_OWNER_OR_SUPERIOR)
    fun page(@PathVariable accountId: Int, model: Model) = "/profile/profile-page"
}