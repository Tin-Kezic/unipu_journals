package hr.unipu.journals.feature.invite

import hr.unipu.journals.feature.account.Account
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository

private const val INVITE = "invite"
private const val ID = "id"
private const val EMAIL = "email"
private const val TARGET = "target"

// invitation-target
private const val ADMIN = "'ADMIN'"
private const val EIC_ON_PUBLICATION = "'EIC_ON_PUBLICATION'"
private const val EIC_ON_MANUSCRIPT = "'EIC_ON_MANUSCRIPT'"
private const val SECTION_EDITOR_ON_SECTION = "'SECTION_EDITOR_ON_SECTION'"
private const val EDITOR_ON_MANUSCRIPT = "'EDITOR_ON_MANUSCRIPT'"
private const val REVIEWER_ON_MANUSCRIPT = "'REVIEWER_ON_MANUSCRIPT'"


interface InviteRepository: Repository<Invite, Int> {
    @Query("SELECT * FROM $INVITE WHERE $TARGET = $ADMIN")
    fun allAdmin(): List<Account>
}