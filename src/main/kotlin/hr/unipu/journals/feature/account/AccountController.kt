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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/accounts")
class AccountController(
    private val accountRepository: AccountRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authorizationService: AuthorizationService,
    private val emailVerificationService: EmailVerificationService
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
    fun register(@ModelAttribute account: AccountDTO): ResponseEntity<String> {
        if (accountRepository.existsByEmail(account.email)) return ResponseEntity.badRequest().body("email taken")
        emailVerificationService.register(account.clean())
        return ResponseEntity.ok("successfully sent registration confirmation email: $account")
    }
    @PutMapping
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ACCOUNT_OWNER_OR_ADMIN)
    fun update(@ModelAttribute request: AccountDTO): ResponseEntity<String> {
        val account = authorizationService.account!!
        if(accountRepository.existsByEmail(request.email) && account.email != request.email) return ResponseEntity.badRequest().body("email taken")
        val rowsAffected = accountRepository.update(account.id, request.clean())
        return if(rowsAffected == 1) return ResponseEntity.ok("successfully updated account $request")
        else ResponseEntity.internalServerError().body("failed to update account: $request")
    }
    @PutMapping("/email")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ACCOUNT_OWNER_OR_ADMIN)
    fun updateEmail(@RequestParam("accountId") accountId: Int, @RequestParam newEmail: String): ResponseEntity<String> {
        emailVerificationService.changeEmail(AccountAndNewEmail(
            accountRepository.byId(accountId) ?: return ResponseEntity.badRequest().body("unauthenticated"),
            newEmail
        ))
        return ResponseEntity.ok("successfully updated email")
    }
    @DeleteMapping
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ACCOUNT_OWNER_OR_ADMIN)
    fun delete(@RequestParam("accountId") accountId: Int): ResponseEntity<String> {
        emailVerificationService.delete(accountRepository.byId(accountId) ?: return ResponseEntity.badRequest().body("failed to find account"))
        return ResponseEntity.ok("successfully sent deletion confirmation email: $accountId")
    }
}