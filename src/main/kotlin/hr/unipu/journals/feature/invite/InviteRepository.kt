package hr.unipu.journals.feature.invite

import hr.unipu.journals.feature.manuscript.Manuscript
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

private const val INVITE = "invite"
private const val ID = "id"
private const val EMAIL = "email"
private const val TARGET = "target"
private const val TARGET_ID = "target_id"

// invitation-target
private const val ADMIN = "'ADMIN'"
private const val EIC_ON_PUBLICATION = "'EIC_ON_PUBLICATION'"
private const val EIC_ON_MANUSCRIPT = "'EIC_ON_MANUSCRIPT'"
private const val SECTION_EDITOR_ON_SECTION = "'SECTION_EDITOR_ON_SECTION'"
private const val EDITOR_ON_MANUSCRIPT = "'EDITOR_ON_MANUSCRIPT'"
private const val REVIEWER_ON_MANUSCRIPT = "'REVIEWER_ON_MANUSCRIPT'"

// manuscript
private const val MANUSCRIPT = "manuscript"
private const val TITLE = "title"
private const val DESCRIPTION = "description"
private const val AUTHOR_ID = "author_id"
private const val CATEGORY_ID = "category_id"
private const val CURRENT_STATE = "current_state"
private const val SECTION_ID = "section_id"
private const val FILE_URL = "file_url"
private const val SUBMISSION_DATE = "submission_date"
private const val PUBLICATION_DATE = "publication_date"
private const val VIEWS = "views"
private const val DOWNLOADS = "downloads"

// section
private const val PUBLICATION_SECTION = "publication_section"
//private const val ID = "id"
//private const val TITLE = "title"
//private const val DESCRIPTION = "description"
private const val PUBLICATION_ID = "publication_id"
private const val IS_HIDDEN = "is_hidden"

// publication
private const val PUBLICATION = "publication"
//private const val ID = "id"
//private const val TITLE = "title"
//private const val IS_HIDDEN = "is_hidden"

interface InviteRepository: Repository<Invite, Int> {

    @Query("SELECT EXISTS (SELECT 1 FROM $INVITE WHERE $EMAIL = :$EMAIL AND $TARGET = $ADMIN)")
    fun isAdmin(@Param(EMAIL) email: String): Boolean

    @Query("SELECT $EMAIL FROM $INVITE WHERE $TARGET = $ADMIN")
    fun allAdminEmails(): List<String>

    @Query("SELECT $INVITE.$EMAIL FROM $INVITE WHERE $INVITE.$TARGET = $EIC_ON_PUBLICATION AND $INVITE.$TARGET_ID = :$ID")
    fun eicOnPublicationEmailsByPublicationId(@Param(ID) publicationId: Int): List<String>

    @Query("""
        SELECT DISTINCT $MANUSCRIPT.* FROM $MANUSCRIPT
        JOIN $PUBLICATION_SECTION ON $MANUSCRIPT.$SECTION_ID = $PUBLICATION_SECTION.$ID
        JOIN $PUBLICATION ON $PUBLICATION_SECTION.$PUBLICATION_ID = $PUBLICATION.$ID
        JOIN $INVITE ON $PUBLICATION.$ID = $INVITE.$TARGET_ID
        WHERE $INVITE.$TARGET = $EIC_ON_PUBLICATION
        AND $PUBLICATION.$IS_HIDDEN = FALSE
        AND $PUBLICATION_SECTION.$IS_HIDDEN = FALSE
    """)
    fun eicOnManuscript(@Param(ID) id: Int): List<Manuscript>

    @Query("""
        SELECT DISTINCT $MANUSCRIPT.* FROM $MANUSCRIPT
        JOIN $PUBLICATION_SECTION ON $MANUSCRIPT.$SECTION_ID = $PUBLICATION_SECTION.$ID
        JOIN $PUBLICATION ON $PUBLICATION_SECTION.$PUBLICATION_ID = $PUBLICATION.$ID
        JOIN $INVITE ON $PUBLICATION.$ID = $INVITE.$TARGET_ID
        WHERE $INVITE.$TARGET = $EIC_ON_PUBLICATION
        AND $PUBLICATION.$IS_HIDDEN = FALSE
        AND $PUBLICATION_SECTION.$IS_HIDDEN = FALSE
        AND $PUBLICATION.$ID = :$PUBLICATION_ID
    """)
    fun eicOnManuscriptByPublication(@Param(ID) id: Int, @Param(PUBLICATION_ID) publicationId: Int): List<Manuscript>

    @Modifying
    @Query("INSERT INTO $INVITE ($EMAIL, $TARGET, $TARGET_ID) VALUES (:$EMAIL, :$TARGET, :$TARGET_ID)")
    fun insert(@Param(EMAIL) email: String, @Param(TARGET) target: InvitationTarget, @Param(TARGET_ID) targetId: Int = -1)

    @Modifying
    @Query("DELETE FROM $INVITE WHERE $EMAIL = :$EMAIL AND $TARGET = :$TARGET AND $TARGET_ID = :$TARGET_ID")
    fun revoke(@Param(EMAIL) email: String, @Param(TARGET) target: InvitationTarget, @Param(TARGET_ID) targetId: Int = -1)

    @Query("SELECT $EMAIL FROM $INVITE WHERE $TARGET = $ADMIN")
    fun allAdminEmails(): List<String>
}