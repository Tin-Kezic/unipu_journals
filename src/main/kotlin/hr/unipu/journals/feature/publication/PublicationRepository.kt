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

// manuscript
private const val MANUSCRIPT = "manuscript"
private const val CURRENT_STATE = "current_state"

interface PublicationRepository: Repository<Publication, Int> {

    /*
    SELECT p.*
FROM publications p
WHERE p.is_visible = TRUE
  AND EXISTS (
      SELECT 1
      FROM sections s
      JOIN manuscripts m ON m.section_id = s.id
      WHERE s.publication_id = p.id
        AND s.is_visible = TRUE
        AND m.state = 'published'
  );

     */

    // view
    @Query("""
        SELECT * FROM $PUBLICATION WHERE $IS_HIDDEN = FALSE
        AND EXISTS (SELECT 1 FROM $PUBLICATION_SECTION
        WHERE $IS_HIDDEN = FALSE AND EXISTS (SELECT 1 FROM
        $MANUSCRIPT WHERE $CURRENT_STATE = 'PUBLISHED'))
        """)
    fun allPublished(): List<Publication>
    /*
    SELECT p.*
    /*
    i
    SELECT p.*
FROM publications p
WHERE p.is_visible = TRUE
  AND EXISTS (
      SELECT 1
      FROM sections s
      JOIN manuscripts m ON m.section_id = s.id
      WHERE s.publication_id = p.id
        AND s.is_visible = TRUE
        AND m.state = 'published'
  );

     */
FROM publications p
WHERE p.is_visible = TRUE
  AND EXISTS (
      SELECT 1
      FROM sections s
      JOIN manuscripts m ON m.section_id = s.id
      WHERE s.publication_id = p.id
        AND s.is_visible = TRUE
        AND m.state = 'published'
  );

     */

    @Query("SELECT * FROM $PUBLICATION")
    fun allArchived(): List<Publication>

    @Query("SELECT * FROM $PUBLICATION")
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