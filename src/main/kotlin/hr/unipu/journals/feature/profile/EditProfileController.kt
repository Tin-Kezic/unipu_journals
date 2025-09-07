package hr.unipu.journals.feature.profile

import hr.unipu.journals.feature.account.AccountRepository
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_ACCOUNT_OWNER_OR_ADMIN
import hr.unipu.journals.security.AuthorizationService
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import java.net.URLEncoder

private const val encoding = "UTF-8"

@Controller
class EditProfileController(
    private val accountRepository: AccountRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authorizationService: AuthorizationService
) {
    @PostMapping("/profile/{accountId}/edit")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ACCOUNT_OWNER_OR_ADMIN)
    fun insert(@PathVariable accountId: Int, @ModelAttribute request: ProfileRequestDTO): String {
        val account = authorizationService.account!!
        val id = account.id
        var errors = ""
        if(accountRepository.emailExists(request.email)) errors += "&email-taken"
        if(request.password != request.passwordConfirmation) errors += "&password-mismatch"
        if(errors.isNotEmpty())
            return "redirect:/profile/$accountId/edit?$errors" +
                    "&fullName=${URLEncoder.encode(request.fullName, encoding)}" +
                    "&title=${URLEncoder.encode(request.title, encoding)}" +
                    "&email=${URLEncoder.encode(request.email, encoding)}" +
                    "&affiliation=${URLEncoder.encode(request.affiliation, encoding)}" +
                    "&jobType=${URLEncoder.encode(request.jobType, encoding)}" +
                    "&country=${URLEncoder.encode(request.country, encoding)}" +
                    "&city=${URLEncoder.encode(request.city, encoding)}" +
                    "&address=${URLEncoder.encode(request.address, encoding)}" +
                    "&zipCode=${URLEncoder.encode(request.zipCode, encoding)}"
        accountRepository.update(
            id = id,
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
        return "redirect:/profile/$accountId"
    }
}
