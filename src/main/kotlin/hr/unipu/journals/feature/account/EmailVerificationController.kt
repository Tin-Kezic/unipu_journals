package hr.unipu.journals.feature.account

import hr.unipu.journals.feature.invite.InvitationTarget
import hr.unipu.journals.feature.invite.InviteRepository
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.ManuscriptRole
import hr.unipu.journals.feature.manuscript.core.ManuscriptRepository
import hr.unipu.journals.feature.publication.eic_on_publication.EicOnPublicationRepository
import hr.unipu.journals.feature.section.section_editor_on_section.SectionEditorOnSectionRepository
import hr.unipu.journals.feature.unregistered_author.UnregisteredAuthorRepository
import org.springframework.cache.CacheManager
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class EmailVerificationController(
    private val cacheManager: CacheManager,
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
        unregisteredAuthorRepository.delete(pending.email)

        val account = accountRepository.byEmail(pending.email) ?: return ResponseEntity.internalServerError().body("failed to insert account")
        inviteRepository.allByEmail(pending.email).forEach { (id, email, target, targetId) -> when(target) {
            InvitationTarget.ADMIN -> accountRepository.updateIsAdmin(account.email, true)
            InvitationTarget.EIC_ON_PUBLICATION -> eicOnPublicationRepository.assign(account.id, targetId)
            InvitationTarget.SECTION_EDITOR -> sectionEditorOnSectionRepository.assign(account.id, targetId)
            InvitationTarget.EIC_ON_MANUSCRIPT -> accountRoleOnManuscriptRepository.assign(ManuscriptRole.EIC, account.id, targetId)
            InvitationTarget.EDITOR -> accountRoleOnManuscriptRepository.assign(ManuscriptRole.EDITOR, account.id, targetId)
            InvitationTarget.REVIEWER -> accountRoleOnManuscriptRepository.assign(ManuscriptRole.REVIEWER, account.id, targetId)
        }}
        inviteRepository.revoke(pending.email)
        return ResponseEntity.ok("successfully verified email")
    }
    @GetMapping("/delete")
    fun delete(@RequestParam token: String): ResponseEntity<String> {
        val cache = cacheManager.getCache("pendingDeletions") ?: return ResponseEntity.status(410).body(expired)
        val pending = cache.get(token, Account::class.java) ?: return ResponseEntity.status(410).body(expired)
        cache.evict(token)
        if(pending.isAdmin) inviteRepository.invite(pending.email, InvitationTarget.ADMIN)
        eicOnPublicationRepository.allAffiliatedPublicationIds(pending.id)?.forEach { id -> inviteRepository.invite(pending.email, InvitationTarget.EIC_ON_PUBLICATION, id) }
        sectionEditorOnSectionRepository.allAffiliatedSectionIds(pending.id)?.forEach { id -> inviteRepository.invite(pending.email, InvitationTarget.SECTION_EDITOR, id) }
        println(pending)
        accountRoleOnManuscriptRepository.allAffiliatedRolesAndManuscriptIds(pending.id)?.forEach { (id, manuscriptId, accountId, accountRole) ->
            println("$id | $manuscriptId | $accountId | $accountRole")
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
        manuscriptRepository.all(accountId = pending.id).map { it.id }.forEach { id -> unregisteredAuthorRepository.insert(
            fullName = pending.fullName,
            email = pending.email,
            country = pending.country,
            affiliation = pending.affiliation,
            manuscriptId = id
        )}
        eicOnPublicationRepository.revoke(pending.id)
        sectionEditorOnSectionRepository.revoke(pending.id)
        accountRoleOnManuscriptRepository.revoke(pending.id)
        val rowsAffected = accountRepository.delete(pending.id)
        return if(rowsAffected == 1) ResponseEntity.ok("successfully deleted account")
        else ResponseEntity.internalServerError().body("failed to delete account")
    }
}