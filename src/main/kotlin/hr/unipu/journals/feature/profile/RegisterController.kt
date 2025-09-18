package hr.unipu.journals.feature.profile

import hr.unipu.journals.feature.account.AccountRepository
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import java.net.URLEncoder

private const val encoding = "UTF-8"

@Controller
class RegisterController(
    private val accountRepository: AccountRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @PostMapping("/register")
    fun insert(@ModelAttribute request: ProfileRequestDTO): String {
        var errors = ""
        if(accountRepository.emailExists(request.email)) errors += "&email-taken"
        if(request.password != request.passwordConfirmation) errors += "&password-mismatch"
        if(errors.isNotEmpty())
            return "redirect:/register.html?$errors" +
                    "&fullName=${URLEncoder.encode(request.fullName, encoding)}" +
                    "&title=${URLEncoder.encode(request.title, encoding)}" +
                    "&email=${URLEncoder.encode(request.email, encoding)}" +
                    "&affiliation=${URLEncoder.encode(request.affiliation, encoding)}" +
                    "&jobType=${URLEncoder.encode(request.jobType, encoding)}" +
                    "&country=${URLEncoder.encode(request.country, encoding)}" +
                    "&city=${URLEncoder.encode(request.city, encoding)}" +
                    "&address=${URLEncoder.encode(request.address, encoding)}" +
                    "&zipCode=${URLEncoder.encode(request.zipCode, encoding)}"
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