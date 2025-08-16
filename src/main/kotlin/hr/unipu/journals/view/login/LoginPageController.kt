package hr.unipu.journals.view.login

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class LoginPageController() {
    @GetMapping("/login")
    fun all() = "/login/login-page"
}