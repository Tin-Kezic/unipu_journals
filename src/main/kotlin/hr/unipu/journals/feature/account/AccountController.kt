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
    fun insert(@ModelAttribute request: RegisterRequestDTO): ResponseEntity<String> {

        if (request.password != request.passwordConfirmation) return ResponseEntity.badRequest().body("password mismatch")
        if (repository.emailExists(request.email)) return ResponseEntity.badRequest().body("email taken")
        repository.insert(
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