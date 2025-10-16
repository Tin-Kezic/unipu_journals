package hr.unipu.journals.feature.section_editor_on_section

import hr.unipu.journals.feature.account.AccountRepository
import hr.unipu.journals.feature.invite.InvitationTarget
import hr.unipu.journals.feature.invite.InviteRepository
import org.springframework.http.ResponseEntity
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
    ): ResponseEntity<String> {
        val rowsAffected = accountRepository.byEmail(email)?.let {
            sectionEditorOnSectionRepository.assign(sectionId, it.id)
        } ?: inviteRepository.invite(
            email = email,
            target = InvitationTarget.SECTION_EDITOR_ON_SECTION,
            targetId = sectionId
        )
        return if(rowsAffected > 0) ResponseEntity.ok("successfully assigned section editor $email on section $sectionId")
        else ResponseEntity.internalServerError().body("failed assigning section editor $email on section $sectionId")
    }
    @PutMapping("{publicationId}/section/{sectionId}/revoke-section-editor")
    fun revoke(
        @PathVariable sectionId: Int,
        @RequestParam email: String
    ): ResponseEntity<String> {
        val rowsAffected = accountRepository.byEmail(email)?.let {
            sectionEditorOnSectionRepository.revoke(sectionId, it.id)
        } ?: inviteRepository.revoke(
            email = email,
            target = InvitationTarget.SECTION_EDITOR_ON_SECTION,
            targetId = sectionId
        )
        return if(rowsAffected > 0) ResponseEntity.ok("successfully revoked section editor $email on section $sectionId")
        else ResponseEntity.internalServerError().body("failed revoking section editor $email on section $sectionId")
    }
}
