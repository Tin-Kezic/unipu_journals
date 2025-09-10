package hr.unipu.journals.feature.manuscript

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

private const val MANUSCRIPT = "manuscript"
private const val ID = "id"
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

// publication
private const val PUBLICATION = "publication"
//private const val ID = "id"
//private const val TITLE = "title"
//private const val IS_HIDDEN = "is_hidden"

// section
private const val PUBLICATION_SECTION = "publication_section"
//private const val ID = "id"
//private const val TITLE = "title"
//private const val DESCRIPTION = "description"
private const val PUBLICATION_ID = "publication_id"
private const val IS_HIDDEN = "is_hidden"

// profile
private const val ACCOUNT = "account"
//private const val ID = "id"
private const val FULL_NAME = "full_name"
private const val EMAIL = "email"
private const val PASSWORD = "password"
private const val AFFILIATION = "affiliation"
private const val JOB_TYPE = "job_type"
private const val COUNTRY = "country"
private const val CITY = "city"
private const val ADDRESS = "address"
private const val ZIP_CODE = "zip_code"
private const val IS_ADMIN = "is_admin"

// eic_on_publication
private const val EIC_ON_PUBLICATION = "eic_on_publication"
//private const val ID = "id"
//private const val PUBLICATION_ID = "publication_id"
private const val EIC_ID = "eic_id"

interface ManuscriptRepository: Repository<Manuscript, Int> {

    @Modifying
    @Transactional
    @Query("UPDATE $MANUSCRIPT SET $VIEWS = $VIEWS + 1 WHERE $ID = :$ID")
    fun incrementViews(@Param(ID) id: Int)

    @Modifying
    @Query("UPDATE $MANUSCRIPT SET $DOWNLOADS = $DOWNLOADS + 1 WHERE $ID = :$ID")
    fun incrementDownloads(@Param(ID) id: Int)

    @Modifying
    @Query("INSERT INTO $MANUSCRIPT ($TITLE, $AUTHOR_ID, $CATEGORY_ID, $SECTION_ID, $FILE_URL) VALUES (:$TITLE, :$AUTHOR_ID, :$CATEGORY_ID, :$SECTION_ID, :$FILE_URL)")
    fun insert(
        @Param(TITLE) title: String,
        @Param(AUTHOR_ID) authorId: Int,
        @Param(CATEGORY_ID) categoryId: Int,
        @Param(SECTION_ID) sectionId: Int,
        @Param(FILE_URL) fileUrl: String
    )
    @Query("SELECT $TITLE from $PUBLICATION_SECTION WHERE $ID = :$ID")
    fun title(@Param(ID) sectionId: Int): String

    @Query("""
        SELECT DISTINCT $MANUSCRIPT.* FROM $MANUSCRIPT
        JOIN $PUBLICATION_SECTION ON $MANUSCRIPT.$SECTION_ID = $PUBLICATION_SECTION.$ID
        JOIN $PUBLICATION ON $PUBLICATION_SECTION.$PUBLICATION_ID = $PUBLICATION.$ID
        JOIN $EIC_ON_PUBLICATION ON $PUBLICATION.$ID = $EIC_ON_PUBLICATION.$PUBLICATION_ID
        WHERE $EIC_ON_PUBLICATION.$EIC_ID = :$ID
        AND $PUBLICATION.$IS_HIDDEN = FALSE
        AND $PUBLICATION_SECTION.$IS_HIDDEN = FALSE
    """)
    fun pending(@Param("id") id: Int): List<Manuscript>

    @Query("""
        SELECT DISTINCT $MANUSCRIPT.* FROM $MANUSCRIPT
        JOIN $PUBLICATION_SECTION ON $MANUSCRIPT.$SECTION_ID = $PUBLICATION_SECTION.$ID
        JOIN $PUBLICATION ON $PUBLICATION_SECTION.$PUBLICATION_ID = $PUBLICATION.$ID
        JOIN $EIC_ON_PUBLICATION ON $PUBLICATION.$ID = $EIC_ON_PUBLICATION.$PUBLICATION_ID
        WHERE $EIC_ON_PUBLICATION.$EIC_ID = :$ID
        AND $PUBLICATION.$IS_HIDDEN = FALSE
        AND $PUBLICATION_SECTION.$IS_HIDDEN = FALSE
        AND $PUBLICATION.$ID = :$PUBLICATION_ID
    """)
    fun pendingByPublication(@Param(ID) id: Int, @Param(PUBLICATION_ID) publicationId: Int): List<Manuscript>

    @Query("SELECT EXISTS (SELECT 1 FROM $MANUSCRIPT WHERE $ID = :$ID)")
    fun exists(@Param(ID) id: Int): Boolean

    @Modifying
    @Query("UPDATE $MANUSCRIPT SET $CURRENT_STATE = $ARCHIVED WHERE $ID = :$ID")
    fun archive(@Param(ID) id: Int)

    @Modifying
    @Query("UPDATE $MANUSCRIPT SET $CURRENT_STATE = $HIDDEN WHERE $ID = :$ID")
    fun hide(@Param(ID) id: Int)

    @Modifying
    @Query("UPDATE $MANUSCRIPT SET $CURRENT_STATE = $PUBLISHED WHERE $ID = :$ID")
    fun publish(@Param(ID) id: Int)

    @Modifying
    @Query("DELETE FROM $MANUSCRIPT WHERE $ID = :$ID")
    fun delete(@Param(ID) id: Int)

    @Query("SELECT * FROM $MANUSCRIPT WHERE $ID = :$ID")
    fun byId(@Param(ID) manuscriptId: Int): Manuscript

    @Query("SELECT * FROM $MANUSCRIPT WHERE $SECTION_ID = :$SECTION_ID AND $CURRENT_STATE = $PUBLISHED ORDER BY $ID DESC")
    fun allPublishedBySectionId(@Param(SECTION_ID) sectionId: Int): List<Manuscript>

    @Query("SELECT * FROM $MANUSCRIPT WHERE $SECTION_ID = :$SECTION_ID AND $CURRENT_STATE = $ARCHIVED ORDER BY $ID DESC")
    fun allArchivedBySectionId(@Param(SECTION_ID) sectionId: Int): List<Manuscript>

    @Query("SELECT * FROM $MANUSCRIPT WHERE $SECTION_ID = :$SECTION_ID AND $CURRENT_STATE = $HIDDEN ORDER BY $ID DESC")
    fun allHiddenBySectionId(@Param(SECTION_ID) sectionId: Int): List<Manuscript>

    @Query("""
        SELECT $MANUSCRIPT.* FROM $MANUSCRIPT
        JOIN $ACCOUNT ON $MANUSCRIPT.$AUTHOR_ID = $ACCOUNT.$ID
        WHERE $MANUSCRIPT.$AUTHOR_ID = $ACCOUNT.$ID
    """)
    fun allByAuthor(@Param(ID) authorId: Int): List<Manuscript>

    @Query("""
        SELECT DISTINCT $MANUSCRIPT.* FROM $MANUSCRIPT
        JOIN $PUBLICATION_SECTION ON $MANUSCRIPT.$SECTION_ID = $PUBLICATION_SECTION.$ID
        JOIN $PUBLICATION ON $PUBLICATION_SECTION.$PUBLICATION_ID = $PUBLICATION.$ID
        WHERE $PUBLICATION_SECTION.$IS_HIDDEN = FALSE
        AND $PUBLICATION.$IS_HIDDEN = FALSE
        AND $MANUSCRIPT.$CURRENT_STATE = $AWAITING_INITIAL_EIC_REVIEW
        OR $MANUSCRIPT.$CURRENT_STATE = $AWAITING_INITIAL_EDITOR_REVIEW
        OR $MANUSCRIPT.$CURRENT_STATE = $AWAITING_REVIEWER_REVIEW
        OR $MANUSCRIPT.$CURRENT_STATE = $MINOR
        OR $MANUSCRIPT.$CURRENT_STATE = $MAJOR
        ORDER BY $MANUSCRIPT.$ID DESC
    """)
    fun allUnderReview(): List<Manuscript>

    @Query("""
        SELECT DISTINCT $MANUSCRIPT.* FROM $MANUSCRIPT
        JOIN $PUBLICATION_SECTION ON $MANUSCRIPT.$SECTION_ID = $PUBLICATION_SECTION.$ID
        JOIN $PUBLICATION ON $PUBLICATION_SECTION.$PUBLICATION_ID = $PUBLICATION.$ID
        WHERE $PUBLICATION_SECTION.$IS_HIDDEN = FALSE
        AND $PUBLICATION.$IS_HIDDEN = FALSE
        AND $MANUSCRIPT.$CURRENT_STATE = $AWAITING_INITIAL_EIC_REVIEW
        OR $MANUSCRIPT.$CURRENT_STATE = $AWAITING_INITIAL_EDITOR_REVIEW
        OR $MANUSCRIPT.$CURRENT_STATE = $AWAITING_REVIEWER_REVIEW
        OR $MANUSCRIPT.$CURRENT_STATE = $MINOR
        OR $MANUSCRIPT.$CURRENT_STATE = $MAJOR
        AND $PUBLICATION.$ID = :$ID
        ORDER BY $MANUSCRIPT.$ID DESC
    """)
    fun allUnderReviewByPublication(@Param(ID) publicationId: Int): List<Manuscript>
}