package hr.unipu.journals.feature.invite

import hr.unipu.journals.feature.manuscript.Manuscript
import hr.unipu.journals.feature.publication.Publication
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

// manuscript-state
private const val AWAITING_INITIAL_EIC_REVIEW = "'AWAITING_INITIAL_EIC_REVIEW'"
private const val AWAITING_INITIAL_EDITOR_REVIEW = "'AWAITING_INITIAL_EDITOR_REVIEW'"
private const val AWAITING_REVIEWER_REVIEW = "'AWAITING_REVIEWER_REVIEW'"
private const val MINOR = "'MINOR'"
private const val MAJOR = "'MAJOR'"
private const val REJECTED = "'REJECTED'"
private const val PUBLISHED = "'PUBLISHED'"
private const val HIDDEN = "'HIDDEN'"
private const val DRAFT = "'DRAFT'"
private const val ARCHIVED = "'ARCHIVED'"

interface InviteRepository: Repository<Invite, Int> {

    @Query("SELECT EXISTS (SELECT 1 FROM $INVITE WHERE $EMAIL = :$EMAIL AND $TARGET = $ADMIN)")
    fun isAdmin(@Param(EMAIL) email: String): Boolean

    @Query("SELECT $EMAIL FROM $INVITE WHERE $TARGET = $ADMIN")
    fun allAdminEmails(): List<String>

    @Query("SELECT $INVITE.$EMAIL FROM $INVITE WHERE $INVITE.$TARGET = $EIC_ON_PUBLICATION AND $INVITE.$TARGET_ID = :$ID")
    fun eicOnPublicationEmailsByPublicationId(@Param(ID) publicationId: Int): List<String>

    @Query("SELECT $INVITE.$EMAIL FROM $INVITE WHERE $INVITE.$TARGET = $SECTION_EDITOR_ON_SECTION AND $INVITE.$TARGET_ID = :$ID")
    fun sectionEditorOnSectionEmailsBySectionId(@Param(ID) sectionId: Int): List<String>

    @Query("""
        SELECT DISTINCT $PUBLICATION.* FROM $INVITE
        JOIN $MANUSCRIPT ON $INVITE.$TARGET_ID = $MANUSCRIPT.$ID
        JOIN $PUBLICATION_SECTION ON $MANUSCRIPT.$SECTION_ID = $PUBLICATION_SECTION.$ID
        JOIN $PUBLICATION ON $PUBLICATION_SECTION.$PUBLICATION_ID = $PUBLICATION.$ID
        WHERE $INVITE.$TARGET IN ($EIC_ON_PUBLICATION, $EIC_ON_MANUSCRIPT, $SECTION_EDITOR_ON_SECTION, $EDITOR_ON_MANUSCRIPT, $REVIEWER_ON_MANUSCRIPT)
        AND $MANUSCRIPT.$CURRENT_STATE IN ($AWAITING_INITIAL_EIC_REVIEW, $AWAITING_INITIAL_EDITOR_REVIEW, $AWAITING_REVIEWER_REVIEW)
        AND $INVITE.$EMAIL = :$EMAIL
        """)
    fun allPublicationsUnderReviewWithAffiliation(@Param(EMAIL) email: String): List<Publication>

    @Query("""
        SELECT DISTINCT $MANUSCRIPT.* FROM $INVITE
        JOIN $MANUSCRIPT ON $INVITE.$TARGET_ID = $MANUSCRIPT.$ID
        JOIN $PUBLICATION_SECTION ON $MANUSCRIPT.$SECTION_ID = $PUBLICATION_SECTION.$ID
        JOIN $PUBLICATION ON $PUBLICATION_SECTION.$PUBLICATION_ID = $PUBLICATION.$ID
        WHERE $INVITE.$TARGET IN ($EIC_ON_MANUSCRIPT, $EDITOR_ON_MANUSCRIPT, $REVIEWER_ON_MANUSCRIPT)
        AND $MANUSCRIPT.$CURRENT_STATE IN ($AWAITING_INITIAL_EIC_REVIEW, $AWAITING_INITIAL_EDITOR_REVIEW, $AWAITING_REVIEWER_REVIEW)
        AND $PUBLICATION.$IS_HIDDEN = FALSE
        AND $PUBLICATION_SECTION.$IS_HIDDEN = FALSE
        AND $INVITE.$EMAIL = :$EMAIL
    """)
    fun affiliatedManuscripts(@Param(EMAIL) email: String): List<Manuscript>

    @Query("""
        SELECT DISTINCT $MANUSCRIPT.* FROM $INVITE
        JOIN $MANUSCRIPT ON $INVITE.$TARGET_ID = $MANUSCRIPT.$ID
        JOIN $PUBLICATION_SECTION ON $MANUSCRIPT.$SECTION_ID = $PUBLICATION_SECTION.$ID
        JOIN $PUBLICATION ON $PUBLICATION_SECTION.$PUBLICATION_ID = $PUBLICATION.$ID
        WHERE $INVITE.$TARGET IN ($EIC_ON_MANUSCRIPT, $EDITOR_ON_MANUSCRIPT, $REVIEWER_ON_MANUSCRIPT)
        AND $MANUSCRIPT.$CURRENT_STATE IN ($AWAITING_INITIAL_EIC_REVIEW, $AWAITING_INITIAL_EDITOR_REVIEW, $AWAITING_REVIEWER_REVIEW)
        AND $PUBLICATION.$IS_HIDDEN = FALSE
        AND $PUBLICATION_SECTION.$IS_HIDDEN = FALSE
        AND $INVITE.$EMAIL = :$EMAIL
        AND $PUBLICATION.$ID = :$PUBLICATION_ID
    """)
    fun affiliatedManuscriptByPublicationId(@Param(EMAIL) email: String, @Param(PUBLICATION_ID) publicationId: Int): List<Manuscript>

    @Modifying
    @Query("INSERT INTO $INVITE ($EMAIL, $TARGET, $TARGET_ID) VALUES (:$EMAIL, :$TARGET, :$TARGET_ID)")
    fun insert(@Param(EMAIL) email: String, @Param(TARGET) target: InvitationTarget, @Param(TARGET_ID) targetId: Int)

    @Modifying
    @Query("DELETE FROM $INVITE WHERE $EMAIL = :$EMAIL AND $TARGET = :$TARGET AND $TARGET_ID = :$TARGET_ID")
    fun revoke(@Param(EMAIL) email: String, @Param(TARGET) target: InvitationTarget, @Param(TARGET_ID) targetId: Int)
}