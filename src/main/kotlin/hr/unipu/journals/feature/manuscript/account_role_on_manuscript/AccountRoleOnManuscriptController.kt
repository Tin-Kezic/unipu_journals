package hr.unipu.journals.feature.manuscript.account_role_on_manuscript

import hr.unipu.journals.EmailService
import hr.unipu.journals.feature.account.AccountRepository
import hr.unipu.journals.feature.invite.InvitationTarget
import hr.unipu.journals.feature.invite.InviteRepository
import hr.unipu.journals.security.AuthorizationService
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/account-role-on-manuscript")
class AccountRoleOnManuscriptController(
    private val accountRepository: AccountRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
    private val inviteRepository: InviteRepository,
    private val authorizationService: AuthorizationService,
    private val emailService: EmailService
) {
    @PostMapping
    fun assign(
        @RequestParam email: String,
        @RequestParam role: ManuscriptRole,
        @RequestParam manuscriptId: Int
    ): ResponseEntity<String> {
        val cleanEmail = Jsoup.clean(email, Safelist.none())
        when(role) {
            ManuscriptRole.EIC -> require(authorizationService.isEicOnManuscript(manuscriptId))
            ManuscriptRole.EDITOR -> require(authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscriptId))
            ManuscriptRole.REVIEWER -> require(authorizationService.isReviewerOnManuscriptOrAffiliatedSuperior(manuscriptId))
            ManuscriptRole.AUTHOR -> return ResponseEntity.badRequest().body("cannot manually assign author to manuscript")
        }
        try {
            accountRepository.byEmail(cleanEmail)?.let {
                accountRoleOnManuscriptRepository.assign(
                    accountRole = role,
                    accountId = it.id,
                    manuscriptId = manuscriptId
                )
            } ?: inviteRepository.invite(
                email = cleanEmail,
                target = InvitationTarget.valueOf(role.name.replace("EIC", "EIC_ON_MANUSCRIPT")),
                targetId = manuscriptId
            )
            return ResponseEntity.ok("successfully assigned role")
        } catch (e: Exception) {
            return if(e.message?.contains("duplicate") ?: false)
                ResponseEntity.badRequest().body("email is already assigned to role")
            else
                ResponseEntity.internalServerError().body("failed to assign role")
        }
    }
}