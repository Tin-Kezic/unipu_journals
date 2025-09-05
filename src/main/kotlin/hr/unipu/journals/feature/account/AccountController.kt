package hr.unipu.journals.feature.account
/*
import hr.unipu.journals.feature.register.RegisterRequestDTO
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/account")
class AccountController(
    private val accountRepository: AccountRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @PostMapping("/insert")
    fun insert(@ModelAttribute request: RegisterRequestDTO): ResponseEntity<String> {
        if (request.password != request.passwordConfirmation) return ResponseEntity.badRequest().body("password mismatch")
        if (accountRepository.emailExists(request.email)) return ResponseEntity.badRequest().body("email taken")
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
        return ResponseEntity.ok("account successfully registered")
    }
    @DeleteMapping("/delete/{id}")
    fun delete(@PathVariable id: Int): ResponseEntity<String> {
        return if(accountRepository.idExists(id)) {
            accountRepository.delete(id)
            ResponseEntity.ok("account deleted successfully")
        } else ResponseEntity.badRequest().body("ID does not exist")
    }
}
 */