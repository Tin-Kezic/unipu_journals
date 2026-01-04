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
    @Value($$"${app.base-url}")
    lateinit var baseUrl: String
    fun register(account: AccountDTO) {
        val token = UUID.randomUUID().toString()
        cacheManager.getCache("pendingRegistrations")?.put(token, account)
        val href = "${baseUrl}/verify?token=${token}"
        val style = "text-decoration:none;background-color:#454545;color:white;border-radius:1rem;padding:1rem;margin:1rem;"
        emailService.sendHtml(account.email, "Email verification", "<a href=\"${href}\" style=\"${style}\">Verify email</a>")
    }
    fun delete(account: Account) {
        val token = UUID.randomUUID().toString()
        cacheManager.getCache("pendingDeletions")?.put(token, account)
        val href = "${baseUrl}/delete?token=${token}"
        val style = "text-decoration:none;background-color:#454545;color:white;border-radius:1rem;padding:1rem;margin:1rem;"
        emailService.sendHtml(account.email, "Account deletion", "<a href=\"${href}\" style=\"${style}\">Permanently delete account</a>")
    }
}