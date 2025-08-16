package hr.unipu.journals.view.login

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class RegisterPageController() {
    @GetMapping("/register")
    fun all() = "/login/register-page"
}