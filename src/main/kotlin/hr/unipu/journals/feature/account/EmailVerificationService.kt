package hr.unipu.journals.feature.account

import hr.unipu.journals.EmailService
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EmailVerificationService(
    private val cacheManager: CacheManager,
    private val emailService: EmailService
) {
    private val style = "text-decoration:none;background-color:#454545;color:white;border-radius:1rem;padding:1rem;margin:1rem;"
    @Value($$"${app.base-url}")
    lateinit var baseUrl: String
    fun register(account: AccountDTO) {
        val token = UUID.randomUUID().toString()
        cacheManager.getCache("pendingRegistrations")?.put(token, account)
        val href = "${baseUrl}/authentication/verify?token=${token}"
        emailService.sendHtml(account.email, "Email verification", "<a href=\"${href}\" style=\"${style}\">Verify email</a>")
    }
    fun delete(account: Account) {
        val token = UUID.randomUUID().toString()
        cacheManager.getCache("pendingDeletions")?.put(token, account)
        val href = "${baseUrl}/authentication/delete?token=${token}"
        emailService.sendHtml(account.email, "Account deletion", "<a href=\"${href}\" style=\"${style}\">Permanently delete account</a>")
    }
    fun changeEmail(accountAndNewEmail: AccountAndNewEmail) {
        val token = UUID.randomUUID().toString()
        cacheManager.getCache("pendingEmailChanges")?.put(token, accountAndNewEmail)
        emailService.sendHtml(accountAndNewEmail.account.email, "Email changed", "<p style=\"margin:1rem;\">your account email has been changed to ${accountAndNewEmail.newEmail}</p>")
        emailService.sendHtml(accountAndNewEmail.newEmail, "Confirm email change", button("change-email", token, "Confirm email change to ${accountAndNewEmail.newEmail}"))
    }
}