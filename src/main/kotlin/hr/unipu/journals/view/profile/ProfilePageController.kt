package hr.unipu.journals.view.profile

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ProfilePageController() {
    @GetMapping("/profile/{accountId}")
    fun page(model: Model) = "/profile/profile-page"
}