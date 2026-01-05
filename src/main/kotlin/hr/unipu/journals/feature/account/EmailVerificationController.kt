package hr.unipu.journals.feature.account

import hr.unipu.journals.feature.invite.InvitationTarget
import hr.unipu.journals.feature.invite.InviteRepository
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.ManuscriptRole
import hr.unipu.journals.feature.manuscript.core.ManuscriptRepository
import hr.unipu.journals.feature.publication.eic_on_publication.EicOnPublicationRepository
import hr.unipu.journals.feature.section.section_editor_on_section.SectionEditorOnSectionRepository
import hr.unipu.journals.feature.unregistered_author.UnregisteredAuthorRepository
import hr.unipu.journals.security.AuthorizationService
import org.springframework.cache.CacheManager
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/authentication")
class EmailVerificationController(
    private val cacheManager: CacheManager,
    private val authorizationService: AuthorizationService,
    private val accountRepository: AccountRepository,
    private val unregisteredAuthorRepository: UnregisteredAuthorRepository,
    private val manuscriptRepository: ManuscriptRepository,
    private val inviteRepository: InviteRepository,
    private val eicOnPublicationRepository: EicOnPublicationRepository,
    private val sectionEditorOnSectionRepository: SectionEditorOnSectionRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository
) {
    val expired = "Verification link expired. Submit a new request to receive a new link."
    @GetMapping("/verify")
    fun verify(@RequestParam token: String): ResponseEntity<String> {
        val cache = cacheManager.getCache("pendingRegistrations") ?: return ResponseEntity.status(410).body(expired)
        val pending = cache.get(token, AccountDTO::class.java) ?: return ResponseEntity.status(410).body(expired)
        cache.evict(token)

        val rowsAffected = accountRepository.insert(pending)
        if(rowsAffected == 0) return ResponseEntity.internalServerError().body("failed to insert account")
        val account = accountRepository.byEmail(pending.email) ?: return ResponseEntity.internalServerError().body("failed to insert account")

        unregisteredAuthorRepository.allAffiliatedManuscriptIds(pending.email).forEach { id ->
            accountRoleOnManuscriptRepository.assign(ManuscriptRole.AUTHOR, account.id, id)
        }
        unregisteredAuthorRepository.delete(account.email)

        inviteRepository.allByEmail(pending.email).forEach { (id, email, target, targetId) -> when(target) {
            InvitationTarget.ADMIN -> accountRepository.updateIsAdmin(account.email, true)
            InvitationTarget.EIC_ON_PUBLICATION -> eicOnPublicationRepository.assign(account.id, targetId)
            InvitationTarget.SECTION_EDITOR -> sectionEditorOnSectionRepository.assign(account.id, targetId)
            InvitationTarget.EIC_ON_MANUSCRIPT -> accountRoleOnManuscriptRepository.assign(ManuscriptRole.EIC, account.id, targetId)
            InvitationTarget.EDITOR -> accountRoleOnManuscriptRepository.assign(ManuscriptRole.EDITOR, account.id, targetId)
            InvitationTarget.REVIEWER -> accountRoleOnManuscriptRepository.assign(ManuscriptRole.REVIEWER, account.id, targetId)
        }}
        inviteRepository.revoke(account.email)
        return ResponseEntity.ok("successfully verified email")
    }
    @GetMapping("/delete")
    fun delete(@RequestParam token: String): ResponseEntity<String> {
        val cache = cacheManager.getCache("pendingDeletions") ?: return ResponseEntity.status(410).body(expired)
        val pending = cache.get(token, Account::class.java) ?: return ResponseEntity.status(410).body(expired)
        cache.evict(token)
        if(pending.isAdmin) inviteRepository.invite(pending.email, InvitationTarget.ADMIN)
        eicOnPublicationRepository.allAffiliatedPublicationIds(pending.id).forEach { id -> inviteRepository.invite(pending.email, InvitationTarget.EIC_ON_PUBLICATION, id) }
        sectionEditorOnSectionRepository.allAffiliatedSectionIds(pending.id).forEach { id -> inviteRepository.invite(pending.email, InvitationTarget.SECTION_EDITOR, id) }
        accountRoleOnManuscriptRepository.allAffiliatedRolesAndManuscriptIds(pending.id).forEach { (id, manuscriptId, accountId, accountRole) ->
            when(accountRole) {
                ManuscriptRole.EIC -> inviteRepository.invite(pending.email, InvitationTarget.EIC_ON_MANUSCRIPT, manuscriptId)
                ManuscriptRole.EDITOR -> inviteRepository.invite(pending.email, InvitationTarget.EDITOR, manuscriptId)
                ManuscriptRole.REVIEWER -> inviteRepository.invite(pending.email, InvitationTarget.REVIEWER, manuscriptId)
                ManuscriptRole.AUTHOR -> unregisteredAuthorRepository.insert(
                    fullName = pending.fullName,
                    email = pending.email,
                    country = pending.country,
                    affiliation = pending.affiliation,
                    manuscriptId = manuscriptId
                )
            }
        }
        eicOnPublicationRepository.revoke(pending.id)
        sectionEditorOnSectionRepository.revoke(pending.id)
        accountRoleOnManuscriptRepository.revoke(pending.id)
        val rowsAffected = accountRepository.delete(pending.id)
        return if(rowsAffected == 1) ResponseEntity.ok("successfully deleted account")
        else ResponseEntity.internalServerError().body("failed to delete account")
    }
    @GetMapping("/change-email")
    fun changeEmail(@RequestParam token: String): ResponseEntity<String> {
        val cache = cacheManager.getCache("pendingEmailChanges") ?: return ResponseEntity.status(410).body(expired)
        val pending = cache.get(token, AccountAndNewEmail::class.java) ?: return ResponseEntity.status(410).body(expired)
        cache.evict(token)
        if(accountRepository.existsByEmail(pending.newEmail)) return ResponseEntity.badRequest().body("email taken")
        if(authorizationService.isAccountOwnerOrAdmin(pending.account.id).not()) return ResponseEntity.status(401).body("unauthorized")
        accountRepository.updateEmail(pending.account.id, pending.newEmail)
        unregisteredAuthorRepository.allAffiliatedManuscriptIds(pending.newEmail).forEach { id ->
            accountRoleOnManuscriptRepository.assign(ManuscriptRole.AUTHOR, pending.account.id, id)
        }
        unregisteredAuthorRepository.delete(pending.newEmail)
        inviteRepository.allByEmail(pending.newEmail).forEach { (id, email, target, targetId) -> when(target) {
            InvitationTarget.ADMIN -> accountRepository.updateIsAdmin(pending.newEmail, true)
            InvitationTarget.EIC_ON_PUBLICATION -> eicOnPublicationRepository.assign(pending.account.id, targetId)
            InvitationTarget.SECTION_EDITOR -> sectionEditorOnSectionRepository.assign(pending.account.id, targetId)
            InvitationTarget.EIC_ON_MANUSCRIPT -> accountRoleOnManuscriptRepository.assign(ManuscriptRole.EIC, pending.account.id, targetId)
            InvitationTarget.EDITOR -> accountRoleOnManuscriptRepository.assign(ManuscriptRole.EDITOR, pending.account.id, targetId)
            InvitationTarget.REVIEWER -> accountRoleOnManuscriptRepository.assign(ManuscriptRole.REVIEWER, pending.account.id, targetId)
        }}
        inviteRepository.revoke(pending.newEmail)
        return ResponseEntity.ok("successfully changed email")
    }
}