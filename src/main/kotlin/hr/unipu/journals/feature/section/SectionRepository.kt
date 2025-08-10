package hr.unipu.journals.feature.section

import hr.unipu.journals.feature.publication.Publication
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

private const val PUBLICATION_SECTION = "publication_section"
private const val TITLE = "title"
private const val DESCRIPTION = "description"


interface SectionRepository: Repository<Section, Int> {
    // view
    @Query("SELECT * FROM $PUBLICATION_SECTION")
    fun all(): List<Publication>

    @Query("SELECT * FROM $PUBLICATION_SECTION WHERE publication_id = :id")
    fun findByPublicationId(@Param("id") id: Int): Publication

    // REST
    @Modifying
    @Query("INSERT INTO $PUBLICATION_SECTION (title, description, publication_id) VALUES (:title, :description, :publicationId)")
    fun insert(
        @Param("title") title: String,
        @Param("description") description: String,
        @Param("publicationId") publicationId: Int,
    )
    @Modifying
    @Query("UPDATE $PUBLICATION_SECTION SET title = :title WHERE id = :id")
    fun updateTitle(@Param("id") id: Int, @Param("title") title: String)

    @Modifying
    @Query("UPDATE $PUBLICATION_SECTION SET title = :title WHERE id = :id")
    fun updateDescription(@Param("id") id: Int, @Param("title") title: String)

    @Query("SELECT EXISTS (SELECT 1 FROM $PUBLICATION_SECTION WHERE id = :id)")
    fun existsById(@Param("id") id: Int): Boolean

    @Modifying
    @Query("UPDATE $PUBLICATION_SECTION SET is_hidden = TRUE where id = :id")
    fun hide(@Param("id") id: Int)

    @Modifying
    @Query("DELETE FROM $PUBLICATION_SECTION WHERE id = :id")
    fun delete(@Param("id") id: Int)
}