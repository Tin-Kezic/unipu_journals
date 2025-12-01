package hr.unipu.journals.feature.section.section_editor_on_section

import hr.unipu.journals.feature.account.AccountRepository
import hr.unipu.journals.feature.invite.InvitationTarget
import hr.unipu.journals.feature.invite.InviteRepository
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_EIC_ON_PUBLICATION_OR_ADMIN
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/publications")
class SectionEditorOnSectionController(
    private val sectionEditorOnSectionRepository: SectionEditorOnSectionRepository,
    private val accountRepository: AccountRepository,
    private val inviteRepository: InviteRepository
) {
    @PutMapping("{publicationId}/sections/{sectionId}/assign-section-editor")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_EIC_ON_PUBLICATION_OR_ADMIN)
    fun assign(
        @PathVariable publicationId: Int,
        @PathVariable sectionId: Int,
        @RequestParam email: String
    ): ResponseEntity<String> {
        try {
            val rowsAffected = accountRepository.byEmail(email)?.let {
                sectionEditorOnSectionRepository.assign(sectionId, it.id)
            } ?: inviteRepository.invite(
                email = email,
                target = InvitationTarget.SECTION_EDITOR_ON_SECTION,
                targetId = sectionId
            )
            return if(rowsAffected == 1) ResponseEntity.ok("successfully assigned section editor $email on section $sectionId")
            else ResponseEntity.internalServerError().body("failed to assign section editor $email on section $sectionId")
        } catch (e: Exception) {
            return if(e.message?.contains("duplicate") ?: false)
                ResponseEntity.badRequest().body("$email is already section editor")
            else
                ResponseEntity.internalServerError().body("failed to assign section editor $email on section $sectionId")
        }
    }
    @DeleteMapping("{publicationId}/sections/{sectionId}/revoke-section-editor")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_EIC_ON_PUBLICATION_OR_ADMIN)
    fun revoke(
        @PathVariable publicationId: Int,
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
        return if(rowsAffected == 1) ResponseEntity.ok("successfully revoked section editor $email on section $sectionId")
        else ResponseEntity.internalServerError().body("failed to revoke section editor $email on section $sectionId")
    }
}
