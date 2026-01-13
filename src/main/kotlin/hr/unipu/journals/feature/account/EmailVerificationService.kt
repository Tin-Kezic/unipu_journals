package hr.unipu.journals.feature.account

import hr.unipu.journals.EmailService
import hr.unipu.journals.util.AppProperties
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EmailVerificationService(
    private val cacheManager: CacheManager,
    private val emailService: EmailService,
    private val appProperties: AppProperties
) {
    private val style = "style=\"text-decoration:none;background-color:#454545;color:white;border-radius:1rem;padding:1rem;margin:1rem;\""
    private fun button(type: String, token: String, text: String) = "<a href=\"${appProperties.baseUrl}/authentication/$type?token=$token\" $style>$text</a>"

    fun register(account: AccountDTO) {
        val token = UUID.randomUUID().toString()
        cacheManager.getCache("pendingRegistrations")?.put(token, account)
        emailService.sendHtml(account.email, "Email verification", button("verify", token,"Verify email"))
    }
    fun delete(account: Account) {
        val token = UUID.randomUUID().toString()
        cacheManager.getCache("pendingDeletions")?.put(token, account)
        emailService.sendHtml(account.email, "Account deletion", button("delete", token, "Permanently delete ${account.email} account"))
    }
    fun changeEmail(accountAndNewEmail: AccountAndNewEmail) {
        val token = UUID.randomUUID().toString()
        cacheManager.getCache("pendingEmailChanges")?.put(token, accountAndNewEmail)
        emailService.sendHtml(accountAndNewEmail.account.email, "Email changed", "<p style=\"margin:1rem;\">your account email has been changed to ${accountAndNewEmail.newEmail}</p>")
        emailService.sendHtml(accountAndNewEmail.newEmail, "Confirm email change", button("change-email", token, "Confirm email change to ${accountAndNewEmail.newEmail}"))
    }
}