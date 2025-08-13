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

interface PublicationRepository: Repository<Publication, Int> {

    // view
    @Query("""
        SELECT DISTINCT $PUBLICATION.* FROM $PUBLICATION 
        JOIN $PUBLICATION_SECTION ON $PUBLICATION.$ID = $PUBLICATION_SECTION.$PUBLICATION_ID
        JOIN $MANUSCRIPT ON $PUBLICATION_SECTION.$ID = $MANUSCRIPT.$SECTION_ID
        WHERE $PUBLICATION.$IS_HIDDEN = FALSE
        AND $PUBLICATION_SECTION.$IS_HIDDEN = FALSE
        AND $MANUSCRIPT.$CURRENT_STATE = 'PUBLISHED'
    """)
    fun allPublished(): List<Publication>

    @Query("""
        SELECT DISTINCT $PUBLICATION.* FROM $PUBLICATION 
        JOIN $PUBLICATION_SECTION ON $PUBLICATION.$ID = $PUBLICATION_SECTION.$PUBLICATION_ID
        JOIN $MANUSCRIPT ON $PUBLICATION_SECTION.$ID = $MANUSCRIPT.$SECTION_ID
        WHERE $PUBLICATION.$IS_HIDDEN = FALSE
        AND $PUBLICATION_SECTION.$IS_HIDDEN = FALSE
        AND $MANUSCRIPT.$CURRENT_STATE = 'ARCHIVED'
    """)
    fun allArchived(): List<Publication>

    @Query("""
        SELECT DISTINCT $PUBLICATION.* FROM $PUBLICATION 
        JOIN $PUBLICATION_SECTION ON $PUBLICATION.$ID = $PUBLICATION_SECTION.$PUBLICATION_ID
        JOIN $MANUSCRIPT ON $PUBLICATION_SECTION.$ID = $MANUSCRIPT.$SECTION_ID
        WHERE $PUBLICATION.$IS_HIDDEN = TRUE
        OR $PUBLICATION_SECTION.$IS_HIDDEN = TRUE
        OR $MANUSCRIPT.$CURRENT_STATE = 'HIDDEN'
    """)
    fun allHidden(): List<Publication>

    // REST
    @Modifying
    @Query("INSERT INTO $PUBLICATION ($TITLE) VALUES (:$TITLE)")
    fun insert(@Param(TITLE) title: String)

    @Modifying
    @Query("UPDATE $PUBLICATION SET $TITLE = :$TITLE WHERE $ID = :$ID")
    fun updateTitle(@Param(ID) id: Int, @Param(TITLE) title: String)

    @Query("SELECT EXISTS (SELECT 1 FROM $PUBLICATION WHERE $ID = :$ID)")
    fun existsById(@Param(ID) id: Int): Boolean

    @Modifying
    @Query("UPDATE $PUBLICATION SET $IS_HIDDEN = :is_hidden WHERE $ID = :$ID")
    fun updateHidden(@Param(ID) id: Int, @Param("is_hidden") isHidden: Boolean)

    @Modifying
    @Query("DELETE FROM $PUBLICATION WHERE $ID = :$ID")
    fun delete(@Param(ID) id: Int)
}