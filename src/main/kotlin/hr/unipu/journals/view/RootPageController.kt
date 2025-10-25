package hr.unipu.journals.view

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
        model["adminEmails"] = accountRepository.allAdminEmails() + inviteRepository.emailsByTarget(InvitationTarget.ADMIN)
        return "manage/root-page"
    }
    @PostMapping("/update-password")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ROOT)
    fun updatePassword(@RequestParam password: String, @RequestParam passwordConfirmation: String): String {
        if(password != passwordConfirmation) return "redirect:/root?password-mismatch"
        val rowsAffected = accountRepository.updateRootPassword(passwordEncoder.encode(password))
        if(rowsAffected == 0) throw InternalServerErrorException("failed to update root password")
        return "redirect:/root?successfully-updated-password"
    }
    @PostMapping("/assign-admin")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ROOT)
    fun assignAdmin(@RequestParam email: String): ResponseEntity<String> {
        var rowsAffected = accountRepository.updateIsAdmin(email, true)
        if(rowsAffected == 0) {
            rowsAffected = inviteRepository.invite(email, InvitationTarget.ADMIN)
            if(rowsAffected == 0) throw InternalServerErrorException("failed to assign admin privileges for $email")
        }
        return ResponseEntity.ok("Successfully added admin privileges to $email")
    }
    @PutMapping("/revoke-admin")
    fun revokeAdmin(@RequestParam email: String): ResponseEntity<String> {
        var rowsAffected = accountRepository.updateIsAdmin(email, false)
        if(rowsAffected == 0) {
            rowsAffected = inviteRepository.revoke(email, InvitationTarget.ADMIN)
            if(rowsAffected == 0) throw InternalServerErrorException("failed to revoke admin privileges for $email")
        }
        return ResponseEntity.ok("Successfully revoked admin privileges for $email")
    }
}