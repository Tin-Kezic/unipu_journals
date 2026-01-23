package hr.unipu.journals.feature.publication.eic_on_publication

import hr.unipu.journals.feature.account.AccountRepository
import hr.unipu.journals.feature.invite.InvitationTarget
import hr.unipu.journals.feature.invite.InviteRepository
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_ADMIN
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/publications/{publicationId}")
@PreAuthorize(AUTHORIZATION_SERVICE_IS_ADMIN)
class EicOnPublicationController(
    private val eicOnPublicationRepository: EicOnPublicationRepository,
    private val accountRepository: AccountRepository,
    private val inviteRepository: InviteRepository
) {
    @PutMapping("/assign-eic")
    fun assign(@PathVariable publicationId: Int, @RequestParam email: String): ResponseEntity<String> {
        try {
            val rowsAffected = accountRepository.byEmail(email)?.let {
                eicOnPublicationRepository.assign(it.id, publicationId)
            } ?: inviteRepository.invite(
                email = email,
                target = InvitationTarget.EIC_ON_PUBLICATION,
                targetId = publicationId
            )
            return if(rowsAffected == 1) ResponseEntity.ok("eic successfully assigned on publication")
            else ResponseEntity.internalServerError().body("failed to assign eic on publication")
        } catch (e: Exception) {
            return if(e.message?.contains("duplicate") ?: false)
                ResponseEntity.badRequest().body("email is already EiC")
            else
                ResponseEntity.internalServerError().body("failed to assign EiC")
        }
    }
    @PutMapping("/revoke-eic")
    fun revoke(@PathVariable publicationId: Int, @RequestParam email: String): ResponseEntity<String> {
        val rowsAffected = accountRepository.byEmail(email)?.let {
            eicOnPublicationRepository.revoke(eicId = it.id, publicationId = publicationId)
        } ?: inviteRepository.revoke(
            email = email,
            target = InvitationTarget.EIC_ON_PUBLICATION,
            targetId = publicationId
        )
        return if(rowsAffected == 1) ResponseEntity.ok("eic successfully revoked on publication")
        else ResponseEntity.internalServerError().body("failed revoking eic on publication")
    }
}
