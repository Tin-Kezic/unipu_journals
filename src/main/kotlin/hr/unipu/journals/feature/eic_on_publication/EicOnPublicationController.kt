package hr.unipu.journals.feature.eic_on_publication

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
class EicOnPublicationController(
    private val eicOnPublicationRepository: EicOnPublicationRepository,
    private val accountRepository: AccountRepository,
    private val inviteRepository: InviteRepository
) {
    @PutMapping("{publicationId}/assign-eic")
    fun assign(@PathVariable publicationId: Int, @RequestParam email: String) {
        if(accountRepository.existsByEmail(email)) {
            val eicId = accountRepository.byEmail(email)!!.id
            eicOnPublicationRepository.assign(publicationId, eicId)
        } else inviteRepository.insert(
            email = email,
            target = InvitationTarget.EIC_ON_PUBLICATION,
            targetId = publicationId
        )
    }

    @PutMapping("{publicationId}/revoke-eic")
    fun revoke(@PathVariable publicationId: Int, @RequestParam email: String) {
        if(accountRepository.existsByEmail(email)) {
            val eicId = accountRepository.byEmail(email)!!.id
            eicOnPublicationRepository.revoke(publicationId, eicId)
        } else inviteRepository.revoke(
            email = email,
            target = InvitationTarget.EIC_ON_PUBLICATION,
            targetId = publicationId
        )
    }
}
