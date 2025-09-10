package hr.unipu.journals.feature.publication

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

private const val PUBLICATION = "publication"
private const val ID = "id"
private const val TITLE = "title"
private const val IS_HIDDEN = "is_hidden"

// publication_section
private const val PUBLICATION_SECTION = "publication_section"
private const val PUBLICATION_ID = "publication_id"

// manuscript
private const val MANUSCRIPT = "manuscript"
private const val SECTION_ID = "section_id"
private const val CURRENT_STATE = "current_state"

// manuscript-state
private const val AWAITING_INITIAL_EIC_REVIEW = "'AWAITING_INITIAL_EIC_REVIEW'"
private const val AWAITING_INITIAL_EDITOR_REVIEW = "'AWAITING_INITIAL_EDITOR_REVIEW'"
private const val AWAITING_REVIEWER_REVIEW = "'AWAITING_REVIEWER_REVIEW'"
private const val MINOR_FIXES = "'MINOR_FIXES'"
private const val MAJOR_FIXES = "'MAJOR_FIXES'"
private const val REJECTED = "'REJECTED'"
private const val PUBLISHED = "'PUBLISHED'"
private const val HIDDEN = "'HIDDEN'"
private const val DRAFT = "'DRAFT'"
private const val ARCHIVED = "'ARCHIVED'"

// account-role-on-manuscript
private const val ACCOUNT_ROLE_ON_MANUSCRIPT = "account_role_on_manuscript"
private const val MANUSCRIPT_ID = "manuscript_id"
private const val ACCOUNT_ID = "account_id"
private const val ACCOUNT_ROLE = "account_role"

// eic-on-publication
private const val EIC_ON_PUBLICATION = "eic_on_publication"
private const val EIC_ID = "eic_id"

// manuscript-role
private const val EIC = "'EIC'"
private const val EDITOR =  "'EDITOR'"
private const val REVIEWER = "'REVIEWER'"
private const val CORRESPONDING_AUTHOR = "'CORRESPONDING_AUTHOR'"
private const val AUTHOR = "'AUTHOR'"

interface PublicationRepository: Repository<Publication, Int> {

    @Query("""
        SELECT DISTINCT $PUBLICATION.* FROM $PUBLICATION
        JOIN $ACCOUNT_ROLE_ON_MANUSCRIPT ON :$EIC_ID = $ACCOUNT_ROLE_ON_MANUSCRIPT.$ACCOUNT_ID
        OR $ACCOUNT_ROLE_ON_MANUSCRIPT.$ACCOUNT_ROLE = $EIC
        OR $ACCOUNT_ROLE_ON_MANUSCRIPT.$ACCOUNT_ROLE = $EDITOR
        OR $ACCOUNT_ROLE_ON_MANUSCRIPT.$ACCOUNT_ROLE = $REVIEWER
        """)
    fun allUnderReviewWithAffiliation(@Param(EIC_ID) eicId: Int): List<Publication>

    @Query("""
        SELECT DISTINCT $PUBLICATION.* FROM $PUBLICATION
        JOIN $EIC_ON_PUBLICATION ON $PUBLICATION.$ID = $EIC_ON_PUBLICATION.$PUBLICATION_ID
        WHERE $PUBLICATION.$ID = $PUBLICATION_ID
        AND $EIC_ON_PUBLICATION.$EIC_ID = :$EIC_ID
    """)
    fun allUnderReviewWithAffiliationByPublicationId(@Param(EIC_ID) eicId: Int, @Param(PUBLICATION_ID) publicationId: Int): List<Publication>

    @Query("SELECT $TITLE FROM $PUBLICATION WHERE $ID = :$ID")
    fun title(@Param(ID) id: Int): String

    @Query("SELECT * FROM $PUBLICATION WHERE $TITLE = :$TITLE")
    fun byTitle(@Param(TITLE) title: String): Publication

    @Query("SELECT * FROM $PUBLICATION WHERE $IS_HIDDEN = FALSE ORDER BY $ID DESC")
    fun allPublished(): List<Publication>

    @Query("""
        SELECT DISTINCT $PUBLICATION.* FROM $PUBLICATION 
        JOIN $PUBLICATION_SECTION ON $PUBLICATION.$ID = $PUBLICATION_SECTION.$PUBLICATION_ID
        JOIN $MANUSCRIPT ON $PUBLICATION_SECTION.$ID = $MANUSCRIPT.$SECTION_ID
        WHERE $PUBLICATION.$IS_HIDDEN = FALSE
        AND $PUBLICATION_SECTION.$IS_HIDDEN = FALSE
        AND $MANUSCRIPT.$CURRENT_STATE = $ARCHIVED
        ORDER BY $PUBLICATION.$ID DESC
    """)
    fun allArchived(): List<Publication>

    @Query("""
        SELECT DISTINCT $PUBLICATION.* FROM $PUBLICATION 
        JOIN $PUBLICATION_SECTION ON $PUBLICATION.$ID = $PUBLICATION_SECTION.$PUBLICATION_ID
        JOIN $MANUSCRIPT ON $PUBLICATION_SECTION.$ID = $MANUSCRIPT.$SECTION_ID
        WHERE $PUBLICATION.$IS_HIDDEN = TRUE
        OR $PUBLICATION_SECTION.$IS_HIDDEN = TRUE
        OR $MANUSCRIPT.$CURRENT_STATE = $HIDDEN
        ORDER BY $PUBLICATION.$ID DESC
    """)
    fun allHidden(): List<Publication>

    @Query("""
        SELECT DISTINCT $PUBLICATION.* FROM $PUBLICATION 
        JOIN $PUBLICATION_SECTION ON $PUBLICATION.$ID = $PUBLICATION_SECTION.$PUBLICATION_ID
        JOIN $MANUSCRIPT ON $PUBLICATION_SECTION.$ID = $MANUSCRIPT.$SECTION_ID
        WHERE $PUBLICATION.$IS_HIDDEN = FALSE
        AND $PUBLICATION_SECTION.$IS_HIDDEN = FALSE
        AND $MANUSCRIPT.$CURRENT_STATE = $AWAITING_INITIAL_EIC_REVIEW
        OR $MANUSCRIPT.$CURRENT_STATE = $AWAITING_INITIAL_EDITOR_REVIEW
        OR $MANUSCRIPT.$CURRENT_STATE = $AWAITING_REVIEWER_REVIEW
        OR $MANUSCRIPT.$CURRENT_STATE = $MINOR_FIXES
        OR $MANUSCRIPT.$CURRENT_STATE = $MAJOR_FIXES
        ORDER BY $PUBLICATION.$ID DESC
    """)
    fun allUnderReview(): List<Publication>

    @Modifying
    @Query("INSERT INTO $PUBLICATION ($TITLE) VALUES (:$TITLE)")
    fun insert(@Param(TITLE) title: String)

    @Modifying
    @Query("UPDATE $PUBLICATION SET $TITLE = :$TITLE WHERE $ID = :$ID")
    fun updateTitle(@Param(ID) id: Int, @Param(TITLE) title: String)

    @Query("SELECT EXISTS (SELECT 1 FROM $PUBLICATION WHERE $ID = :$ID)")
    fun exists(@Param(ID) id: Int): Boolean

    @Modifying
    @Query("UPDATE $PUBLICATION SET $IS_HIDDEN = :$IS_HIDDEN WHERE $ID = :$ID")
    fun updateHidden(@Param(ID) id: Int, @Param(IS_HIDDEN) isHidden: Boolean)

    @Modifying
    @Query("DELETE FROM $PUBLICATION WHERE $ID = :$ID")
    fun delete(@Param(ID) id: Int)
}