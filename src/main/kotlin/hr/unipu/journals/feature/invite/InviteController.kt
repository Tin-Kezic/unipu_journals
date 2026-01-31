package hr.unipu.journals.feature.invite

import hr.unipu.journals.feature.account.AccountRepository
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.ManuscriptRole
import hr.unipu.journals.feature.publication.eic_on_publication.EicOnPublicationRepository
import hr.unipu.journals.feature.section.section_editor_on_section.SectionEditorOnSectionRepository
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_AUTHENTICATED
import hr.unipu.journals.security.AuthorizationService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/invite")
class InviteController(
    private val inviteRepository: InviteRepository,
    private val accountRepository: AccountRepository,
    private val eicOnPublicationRepository: EicOnPublicationRepository,
    private val sectionRepository: SectionEditorOnSectionRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
    private val authorizationService: AuthorizationService
) {
    @PutMapping
    @Transactional
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_AUTHENTICATED)
    fun decide(@RequestParam accept: Boolean, @RequestParam target: InvitationTarget, targetId: Int): ResponseEntity<String> {
        val account = authorizationService.account!!
        if(accept) when(target) {
            InvitationTarget.ADMIN ->
                accountRepository.updateIsAdmin(account.email, true)
            InvitationTarget.EIC_ON_PUBLICATION ->
                eicOnPublicationRepository.assign(account.id, targetId)
            InvitationTarget.SECTION_EDITOR ->
                sectionRepository.assign(account.id,  targetId)
            InvitationTarget.EIC_ON_MANUSCRIPT,
            InvitationTarget.EDITOR,
            InvitationTarget.REVIEWER ->
                accountRoleOnManuscriptRepository.assign(
                    accountRole = ManuscriptRole.valueOf(target.name.replace("_ON_MANUSCRIPT", "")),
                    accountId = account.id,
                    manuscriptId = targetId
                )
        }
        if(accept && target == InvitationTarget.EIC_ON_MANUSCRIPT)
            inviteRepository.revoke(target = InvitationTarget.EIC_ON_MANUSCRIPT, targetId = targetId)
        else inviteRepository.revoke(
            email = account.email,
            target = target,
            targetId = targetId
        )
        return if(accept) ResponseEntity.ok("successfully accepted invite")
        else ResponseEntity.ok("successfully declined invite")
    }
}