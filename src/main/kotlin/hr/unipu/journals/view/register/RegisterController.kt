package hr.unipu.journals.view.register

import hr.unipu.journals.feature.account.AccountRepository
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping

@Controller
class RegisterController(
    private val accountRepository: AccountRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @PostMapping("/register")
    fun insert(@ModelAttribute request: RegisterRequestDTO): String {
        if (request.password != request.passwordConfirmation) return "redirect:/register.html?password-mismatch"
        if (accountRepository.emailExists(request.email)) return "redirect:/register.html?email-taken"
        accountRepository.insert(
            fullName = Jsoup.clean(request.fullName, Safelist.none()),
            title = Jsoup.clean(request.title, Safelist.none()),
            email = Jsoup.clean(request.email, Safelist.none()),
            password = passwordEncoder.encode(request.password),
            affiliation = Jsoup.clean(request.affiliation, Safelist.none()),
            jobType = Jsoup.clean(request.jobType, Safelist.none()),
            country = Jsoup.clean(request.country, Safelist.none()),
            city = Jsoup.clean(request.city, Safelist.none()),
            address = Jsoup.clean(request.address, Safelist.none()),
            zipCode = Jsoup.clean(request.zipCode, Safelist.none())
        )
        return "redirect:/login.html"
    }
}
