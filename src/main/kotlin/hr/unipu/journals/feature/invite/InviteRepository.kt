package hr.unipu.journals.feature.invite

import hr.unipu.journals.feature.account.Account
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

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

    @Modifying
    @Query("INSERT INTO $INVITE ($EMAIL, $TARGET) VALUES (:$EMAIL, :$TARGET)")
    fun insert(@Param(EMAIL) email: String, @Param(TARGET) target: InvitationTarget)

    @Query("SELECT EXISTS (SELECT 1 FROM $INVITE WHERE $EMAIL = :$EMAIL AND $TARGET = $ADMIN)")
    fun isAdmin(@Param(EMAIL) email: String): Boolean

    @Modifying
    @Query("DELETE FROM $INVITE WHERE $EMAIL = :$EMAIL AND $TARGET = :$TARGET")
    fun revoke(@Param(EMAIL) email: String, @Param(TARGET) target: InvitationTarget)

    @Query("SELECT $EMAIL FROM $INVITE WHERE $TARGET = $ADMIN")
    fun allAdminEmails(): List<String>
}