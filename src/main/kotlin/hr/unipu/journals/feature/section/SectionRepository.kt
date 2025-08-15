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


interface SectionRepository: Repository<Section, Int> {

    @Query("SELECT $TITLE from $PUBLICATION_SECTION WHERE $ID = :$ID")
    fun titleById(@Param(ID) sectionId: Int): String

    @Query("SELECT * FROM $PUBLICATION_SECTION WHERE $PUBLICATION_ID = :$PUBLICATION_ID")
    fun allByPublicationId(@Param(PUBLICATION_ID) publicationId: Int): List<Section>

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
    fun existsById(@Param(ID) id: Int): Boolean

    @Modifying
    @Query("UPDATE $PUBLICATION_SECTION SET $IS_HIDDEN = :$IS_HIDDEN WHERE $ID = :$ID")
    fun updateHidden(@Param(ID) id: Int, @Param(IS_HIDDEN) isHidden: Boolean)

    @Modifying
    @Query("DELETE FROM $PUBLICATION_SECTION WHERE $ID = :$ID")
    fun delete(@Param(ID) id: Int)
}