package hr.unipu.journals.feature.manuscript

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

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
private const val MINOR_FIXES = "'MINOR_FIXES'"
private const val MAJOR_FIXES = "'MAJOR_FIXES'"
private const val REJECTED = "'REJECTED'"
private const val PUBLISHED = "'PUBLISHED'"
private const val HIDDEN = "'HIDDEN'"
private const val DRAFT = "'DRAFT'"
private const val ARCHIVED = "'ARCHIVED'"

// publication
private const val PUBLICATION = "publication"

// section
private const val PUBLICATION_SECTION = "publication_section"
private const val PUBLICATION_ID = "publication_id"
private const val IS_HIDDEN = "is_hidden"

interface ManuscriptRepository: Repository<Manuscript, Int> {
    @Query("SELECT * FROM $MANUSCRIPT WHERE $ID = :$ID")
    fun byId(@Param(ID) manuscriptId: Int): Manuscript

    @Query("SELECT * FROM $MANUSCRIPT WHERE $SECTION_ID = :$SECTION_ID AND $CURRENT_STATE = $PUBLISHED ORDER BY $ID DESC")
    fun allPublishedBySectionId(@Param(SECTION_ID) sectionId: Int): List<Manuscript>

    @Query("SELECT * FROM $MANUSCRIPT WHERE $SECTION_ID = :$SECTION_ID AND $CURRENT_STATE = $ARCHIVED ORDER BY $ID DESC")
    fun allArchivedBySectionId(@Param(SECTION_ID) sectionId: Int): List<Manuscript>

    @Query("SELECT * FROM $MANUSCRIPT WHERE $SECTION_ID = :$SECTION_ID AND $CURRENT_STATE = $HIDDEN ORDER BY $ID DESC")
    fun allHiddenBySectionId(@Param(SECTION_ID) sectionId: Int): List<Manuscript>

    @Query("""
        SELECT DISTINCT $MANUSCRIPT.* FROM $MANUSCRIPT
        JOIN $PUBLICATION_SECTION ON $MANUSCRIPT.$SECTION_ID = $PUBLICATION_SECTION.$ID
        JOIN $PUBLICATION ON $PUBLICATION_SECTION.$PUBLICATION_ID = $PUBLICATION.$ID
        WHERE $PUBLICATION_SECTION.$IS_HIDDEN = FALSE
        AND $PUBLICATION.$IS_HIDDEN = FALSE
        AND $MANUSCRIPT.$CURRENT_STATE = $AWAITING_INITIAL_EIC_REVIEW
        OR $MANUSCRIPT.$CURRENT_STATE = $AWAITING_INITIAL_EDITOR_REVIEW
        OR $MANUSCRIPT.$CURRENT_STATE = $AWAITING_REVIEWER_REVIEW
        OR $MANUSCRIPT.$CURRENT_STATE = $MINOR_FIXES
        OR $MANUSCRIPT.$CURRENT_STATE = $MAJOR_FIXES
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
        OR $MANUSCRIPT.$CURRENT_STATE = $MINOR_FIXES
        OR $MANUSCRIPT.$CURRENT_STATE = $MAJOR_FIXES
        AND $PUBLICATION.$ID = :$ID
        ORDER BY $MANUSCRIPT.$ID DESC
    """)
    fun allUnderReviewByPublication(@Param(ID) publicationId: Int): List<Manuscript>
}