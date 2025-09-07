package hr.unipu.journals.feature.eic_on_publication

import hr.unipu.journals.feature.account.AccountRepository
import hr.unipu.journals.feature.invite.InvitationTarget
import hr.unipu.journals.feature.invite.InviteRepository
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/publication")
class EicOnPublicationController(
    private val eicOnPublicationRepository: EicOnPublicationRepository,
    private val accountRepository: AccountRepository,
    private val inviteRepository: InviteRepository
) {
    @PostMapping("{publicationId}/assign")
    fun insert(@PathVariable publicationId: Int, @RequestParam email: String) {
        if(accountRepository.emailExists(email)) {
            val eicId = accountRepository.byEmail(email)!!.id
            eicOnPublicationRepository.assign(publicationId, eicId)
        } else inviteRepository.insert(
            email = email,
            target = InvitationTarget.EIC_ON_PUBLICATION,
            targetId = publicationId
        )
    }

    @DeleteMapping("{publicationId}/revoke")
    fun delete(@PathVariable publicationId: Int, @RequestParam email: String) {
        if(accountRepository.emailExists(email)) {
            val eicId = accountRepository.byEmail(email)!!.id
            eicOnPublicationRepository.revoke(publicationId, eicId)
        } else inviteRepository.insert(
            email = email,
            target = InvitationTarget.EIC_ON_PUBLICATION,
            targetId = publicationId
        )
    }
}
