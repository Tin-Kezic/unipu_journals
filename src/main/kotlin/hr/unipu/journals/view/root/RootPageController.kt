package hr.unipu.journals.view.root

import hr.unipu.journals.feature.account.AccountRepository
import hr.unipu.journals.feature.invite.InvitationTarget
import hr.unipu.journals.feature.invite.InviteRepository
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_ROOT
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/root")
class RootPageController(
    private val passwordEncoder: PasswordEncoder,
    private val inviteRepository: InviteRepository,
    private val accountRepository: AccountRepository,
) {
    @GetMapping
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ROOT)
    fun page(model: Model): String {
        model["adminEmails"] = accountRepository.allAdminEmails() + inviteRepository.allAdminEmails()
        return "configure/root-page"
    }

    @PostMapping("/update-password")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ROOT)
    fun updatePassword(@RequestParam password: String, @RequestParam passwordConfirmation: String): String {
        if(password != passwordConfirmation) return "redirect:/root?password-mismatch"
        accountRepository.updateRootPassword(passwordEncoder.encode(password))
        return "redirect:/root?successfully-updated-password"
    }

    @PostMapping("/add-admin")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ROOT)
    fun addAdmin(@RequestParam email: String): ResponseEntity<String> {
        if(accountRepository.emailExists(email)) accountRepository.updateIsAdmin(email, true)
        else inviteRepository.insert(email, InvitationTarget.ADMIN)
        return ResponseEntity.ok("Successfully added admin privileges to $email")
    }

    @PutMapping("/revoke-admin")
    fun revokeAdmin(@RequestParam email: String): ResponseEntity<String> {
        if(accountRepository.isAdmin(email)) accountRepository.updateIsAdmin(email, false)
        else if (inviteRepository.isAdmin(email)) inviteRepository.revoke(email, InvitationTarget.ADMIN)
        else return ResponseEntity.status(404).body("No admin entry found for email: $email")
        return ResponseEntity.ok("Successfully revoked admin privileges for $email")
    }
}