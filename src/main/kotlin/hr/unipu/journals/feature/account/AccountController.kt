package hr.unipu.journals.feature.account

import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_ACCOUNT_OWNER_OR_ADMIN
import hr.unipu.journals.security.AuthorizationService
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/account")
class AccountController(
    private val accountRepository: AccountRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authorizationService: AuthorizationService
) {
    @PostMapping("/insert")
    fun insert(@ModelAttribute account: AccountDTO): ResponseEntity<String> {
        var error = ""
        if (accountRepository.emailExists(account.email)) error += "email taken"
        if (account.password != account.passwordConfirmation) error += " and password mismatch"
        if(error.isNotEmpty()) return ResponseEntity.badRequest().body(error)
        accountRepository.insert(
            fullName = Jsoup.clean(account.fullName, Safelist.none()),
            title = Jsoup.clean(account.title, Safelist.none()),
            email = Jsoup.clean(account.email, Safelist.none()),
            password = passwordEncoder.encode(account.password),
            affiliation = Jsoup.clean(account.affiliation, Safelist.none()),
            jobType = Jsoup.clean(account.jobType, Safelist.none()),
            country = Jsoup.clean(account.country, Safelist.none()),
            city = Jsoup.clean(account.city, Safelist.none()),
            address = Jsoup.clean(account.address, Safelist.none()),
            zipCode = Jsoup.clean(account.zipCode, Safelist.none())
        )
        return ResponseEntity.ok("account successfully registered")
    }
    @PutMapping("/update")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ACCOUNT_OWNER_OR_ADMIN)
    fun update(@ModelAttribute request: AccountDTO): ResponseEntity<String> {
        val account = authorizationService.account!!
        val currentEmail = account.email
        val id = account.id
        var error = ""
        if(accountRepository.emailExists(request.email) && currentEmail != request.email) error += "email taken"
        if(request.password != request.passwordConfirmation) error += " and password mismatch"
        if(error.isNotEmpty()) return ResponseEntity.badRequest().body(error)
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
        return ResponseEntity.ok("account successfully updated")
    }
    @DeleteMapping("/delete/{id}")
    fun delete(@PathVariable id: Int): ResponseEntity<String> {
        return if(accountRepository.exists(id)) {
            accountRepository.delete(id)
            ResponseEntity.ok("account deleted successfully")
        } else ResponseEntity.badRequest().body("ID does not exist")
    }
}