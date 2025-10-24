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
    fun AccountDTO.clean() = this.copy(
        fullName = Jsoup.clean(this.fullName, Safelist.none()),
        title = Jsoup.clean(this.title, Safelist.none()),
        email = Jsoup.clean(this.email, Safelist.none()),
        password = passwordEncoder.encode(this.password),
        affiliation = Jsoup.clean(this.affiliation, Safelist.none()),
        jobType = Jsoup.clean(this.jobType, Safelist.none()),
        country = Jsoup.clean(this.country, Safelist.none()),
        city = Jsoup.clean(this.city, Safelist.none()),
        address = Jsoup.clean(this.address, Safelist.none()),
        zipCode = Jsoup.clean(this.zipCode, Safelist.none())
    )
    @PostMapping
    fun insert(@ModelAttribute account: AccountDTO): ResponseEntity<String> {
        var error = ""
        if (accountRepository.existsByEmail(account.email)) error += "email taken"
        if (account.password != account.passwordConfirmation) error += " and password mismatch"
        if(error.isNotEmpty()) return ResponseEntity.badRequest().body(error)
        val rowsInserted = accountRepository.insert(account.clean())
        if(rowsInserted > 0) return ResponseEntity.ok("successfully registered account: $account")
        return ResponseEntity.internalServerError().body("failed to register account: $account")
    }
    @PutMapping
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ACCOUNT_OWNER_OR_ADMIN)
    fun update(@ModelAttribute request: AccountDTO): ResponseEntity<String> {
        val account = authorizationService.account!!
        val currentEmail = account.email
        val id = account.id
        var error = ""
        if(accountRepository.existsByEmail(request.email) && currentEmail != request.email) error += "email taken"
        if(request.password != request.passwordConfirmation) error += " and password mismatch"
        if(error.isNotEmpty()) return ResponseEntity.badRequest().body(error)
        val rowsAffected = accountRepository.update(id, request.clean())
        return if(rowsAffected == 1) return ResponseEntity.ok("successfully updated account $request")
            else ResponseEntity.internalServerError().body("failed to update account: $request")
    }
    @DeleteMapping
    fun delete(@PathVariable id: Int): ResponseEntity<String> {
        val rowsAffected = accountRepository.delete(id)
        return if(rowsAffected == 1) ResponseEntity.ok("successfully deleted account $id")
        else ResponseEntity.internalServerError().body("failed to delete account $id")
    }
}