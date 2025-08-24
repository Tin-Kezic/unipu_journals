package hr.unipu.journals.feature.section

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

private const val PUBLICATION_SECTION = "publication_section"
private const val ID = "id"
private const val TITLE = "title"
private const val DESCRIPTION = "description"
private const val PUBLICATION_ID = "publication_id"
private const val IS_HIDDEN = "is_hidden"

// manuscript
private const val MANUSCRIPT = "manuscript"
private const val SECTION_ID = "section_id"
private const val CURRENT_STATE = "current_state"

// manuscript-state
private const val ARCHIVED = "'ARCHIVED'"
private const val HIDDEN = "'HIDDEN'"

interface SectionRepository: Repository<Section, Int> {

    @Query("SELECT $TITLE from $PUBLICATION_SECTION WHERE $ID = :$ID")
    fun title(@Param(ID) sectionId: Int): String

    @Query("SELECT * FROM $PUBLICATION_SECTION WHERE $PUBLICATION_ID = :$PUBLICATION_ID AND $IS_HIDDEN = FALSE")
    fun allPublishedByPublicationId(@Param(PUBLICATION_ID) publicationId: Int): List<Section>

    @Query("""
        SELECT DISTINCT $PUBLICATION_SECTION.* FROM $PUBLICATION_SECTION
        JOIN $MANUSCRIPT ON $PUBLICATION_SECTION.$ID = $MANUSCRIPT.$SECTION_ID
        WHERE $PUBLICATION_SECTION.$IS_HIDDEN = FALSE AND $MANUSCRIPT.$CURRENT_STATE = $ARCHIVED
    """)
    fun allArchivedByPublicationId(@Param(PUBLICATION_ID) publicationId: Int): List<Section>

    @Query("""
        SELECT DISTINCT $PUBLICATION_SECTION.* FROM $PUBLICATION_SECTION
        JOIN $MANUSCRIPT ON $PUBLICATION_SECTION.$ID = $MANUSCRIPT.$SECTION_ID
        WHERE $PUBLICATION_SECTION.$IS_HIDDEN = FALSE AND $MANUSCRIPT.$CURRENT_STATE = $HIDDEN
    """)
    fun allHiddenByPublicationId(@Param(PUBLICATION_ID) publicationId: Int): List<Section>

    @Modifying
    @Query("""
        INSERT INTO $PUBLICATION_SECTION ($TITLE, $DESCRIPTION, $PUBLICATION_ID)
        VALUES (:$TITLE, :$DESCRIPTION, :$PUBLICATION_ID)
    """)
    fun insert(
        @Param(TITLE) title: String,
        @Param(DESCRIPTION) description: String,
        @Param(PUBLICATION_ID) publicationId: Int,
    )
    @Modifying
    @Query("UPDATE $PUBLICATION_SECTION SET $TITLE = :$TITLE, $DESCRIPTION = :$DESCRIPTION WHERE $ID = :$ID")
    fun updateTitleAndDescription(
        @Param(ID) id: Int,
        @Param(TITLE) title: String,
        @Param(DESCRIPTION) description: String
    )

    @Query("SELECT EXISTS (SELECT 1 FROM $PUBLICATION_SECTION WHERE $ID = :$ID)")
    fun exists(@Param(ID) id: Int): Boolean

    @Modifying
    @Query("UPDATE $PUBLICATION_SECTION SET $IS_HIDDEN = :$IS_HIDDEN WHERE $ID = :$ID")
    fun updateHidden(@Param(ID) id: Int, @Param(IS_HIDDEN) isHidden: Boolean)

    @Modifying
    @Query("DELETE FROM $PUBLICATION_SECTION WHERE $ID = :$ID")
    fun delete(@Param(ID) id: Int)
}