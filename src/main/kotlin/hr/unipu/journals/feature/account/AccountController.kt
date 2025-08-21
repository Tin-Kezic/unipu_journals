package hr.unipu.journals.feature.account

import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/account")
class AccountController(
    private val repository: AccountRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @PostMapping("/insert")
    fun insert(
        @ModelAttribute fullName: String,
        @ModelAttribute title: String,
        @ModelAttribute email: String,
        @ModelAttribute password: String,
        @ModelAttribute passwordConfirmation: String,
        @ModelAttribute affiliation: String,
        @ModelAttribute jobType: String,
        @ModelAttribute country: String,
        @ModelAttribute city: String,
        @ModelAttribute address: String,
        @ModelAttribute zipcode: String,
    ): ResponseEntity<String> {
        if (password != passwordConfirmation) return ResponseEntity.badRequest().body("password_mismatch")
        if (repository.emailExists(email)) return ResponseEntity.badRequest().body("email_taken")
        repository.insert(
            fullName = Jsoup.clean(fullName, Safelist.none()),
            title = Jsoup.clean(title, Safelist.none()),
            email = Jsoup.clean(email, Safelist.none()),
            password = passwordEncoder.encode(password),
            affiliation = Jsoup.clean(affiliation, Safelist.none()),
            jobType = Jsoup.clean(jobType, Safelist.none()),
            country = Jsoup.clean(country, Safelist.none()),
            city = Jsoup.clean(city, Safelist.none()),
            address = Jsoup.clean(address, Safelist.none()),
            zipCode = Jsoup.clean(zipcode, Safelist.none())
        )
        return ResponseEntity.ok().body("account successfully registered")
    }
    @PostMapping("/delete/{id}")
    fun delete(@PathVariable id: Int) {
        try {
            //repository.deleteById(id)
            ResponseEntity.ok().body("account deleted successfully")
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().body("Invalid account data. ID must be non-null")
        } catch (_: OptimisticLockingFailureException) {
            ResponseEntity.internalServerError().body("internal server error. OptimisticLockingFailureException")
        }
    }
}