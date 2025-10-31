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
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/root")
@PreAuthorize(AUTHORIZATION_SERVICE_IS_ROOT)
class RootPageController(
    private val passwordEncoder: PasswordEncoder,
    private val inviteRepository: InviteRepository,
    private val accountRepository: AccountRepository,
) {
    @GetMapping
    fun page(model: Model): String {
        model["adminEmails"] = accountRepository.allAdminEmails() + inviteRepository.emailsByTarget(InvitationTarget.ADMIN)
        return "root-page"
    }
    @PutMapping("/update-password")
    fun updatePassword(@RequestBody body: Map<String, String>): ResponseEntity<String> {
        val password = body["password"]
        val passwordConfirmation = body["passwordConfirmation"]
        if(password != passwordConfirmation) return ResponseEntity.badRequest().body("password-mismatch")
        val rowsAffected = accountRepository.updateRootPassword(passwordEncoder.encode(password))
        return if(rowsAffected == 1) ResponseEntity.ok("successfully updated password")
        else ResponseEntity.internalServerError().body("failed to update root password")
    }
    @PutMapping("/assign-admin")
    fun assignAdmin(@RequestParam email: String): ResponseEntity<String> {
        try {
            var rowsAffected = accountRepository.updateIsAdmin(email, true)
            if(rowsAffected == 0) {
                rowsAffected = inviteRepository.invite(email, InvitationTarget.ADMIN)
                if(rowsAffected == 0) return ResponseEntity.internalServerError().body("failed to assign admin privileges for $email")
            }
            return ResponseEntity.ok("Successfully added admin privileges to $email")
        } catch (_: Exception) { return ResponseEntity.badRequest().body("account already has admin privileges") }
    }
    @PutMapping("/revoke-admin")
    fun revokeAdmin(@RequestParam email: String): ResponseEntity<String> {
        var rowsAffected = accountRepository.updateIsAdmin(email, false)
        if(rowsAffected == 0) {
            rowsAffected = inviteRepository.revoke(email, InvitationTarget.ADMIN)
            if(rowsAffected == 0) return ResponseEntity.internalServerError().body("failed to revoke admin privileges for $email")
        }
        return ResponseEntity.ok("Successfully revoked admin privileges for $email")
    }
}