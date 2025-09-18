package hr.unipu.journals.feature.section_editor_on_section

import hr.unipu.journals.feature.account.AccountRepository
import hr.unipu.journals.feature.invite.InvitationTarget
import hr.unipu.journals.feature.invite.InviteRepository
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/publication")
class SectionEditorOnSectionController(
    private val sectionEditorOnSectionRepository: SectionEditorOnSectionRepository,
    private val accountRepository: AccountRepository,
    private val inviteRepository: InviteRepository
) {
    @PutMapping("{publicationId}/section/{sectionId}/assign-section-editor")
    fun assign(
        @PathVariable sectionId: Int,
        @RequestParam email: String
    ) {
        if(accountRepository.emailExists(email)) {
            val sectionEditorId = accountRepository.byEmail(email)!!.id
            sectionEditorOnSectionRepository.assign(sectionId, sectionEditorId)
        } else inviteRepository.insert(
            email = email,
            target = InvitationTarget.SECTION_EDITOR_ON_SECTION,
            targetId = sectionId
        )
    }
    @PutMapping("{publicationId}/section/{sectionId}/revoke-section-editor")
    fun revoke(
        @PathVariable sectionId: Int,
        @RequestParam email: String
    ) {
        if(accountRepository.emailExists(email)) {
            val sectionEditorId = accountRepository.byEmail(email)!!.id
            sectionEditorOnSectionRepository.revoke(sectionId, sectionEditorId)
        } else inviteRepository.revoke(
            email = email,
            target = InvitationTarget.SECTION_EDITOR_ON_SECTION,
            targetId = sectionId
        )
    }
}
