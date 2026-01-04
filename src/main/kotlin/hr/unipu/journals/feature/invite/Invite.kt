package hr.unipu.journals.feature.invite

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("invite")
data class Invite(
    @Id val id: Int,
    val email: String,
    val target: InvitationTarget,
    val targetId: Int
)
