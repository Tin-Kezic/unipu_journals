package hr.unipu.journals.feature.section

import hr.unipu.journals.feature.manuscript.ManuscriptState
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface SectionRepository: Repository<Section, Int> {

    @Query("SELECT title from publication_section WHERE id = :id")
    fun title(@Param("id") sectionId: Int): String

    @Query("""
        SELECT publication_section.title FROM publication_section
        JOIN publication ON publication_section.publication_id = publication.id
        WHERE publication.title = :title
        """)
    fun titleByPublicationTitle(@Param("title") publicationTitle: String): List<String>

    @Query("SELECT description from publication_section WHERE id = :id")
    fun description(@Param("id") sectionId: Int): String

    @Query("""
        SELECT DISTINCT publication_section.* FROM publication_section
        JOIN manuscript ON manuscript.section_id = publication_section.id
        WHERE publication_section.publication_id = :publication_id
        AND (
            :state IS NULL
            OR (
                (publication_section.is_hidden = TRUE OR manuscript.current_state = 'HIDDEN')
                AND :state = 'HIDDEN'
            )
        )
        ORDER BY publication_section.id DESC
    """)
    fun allByPublicationId(@Param("publication_id") publicationId: Int, @Param("state") manuscriptState: ManuscriptState? = null): List<Section>

    @Modifying
    @Query("""
        INSERT INTO publication_section (title, publication_id)
        VALUES (:title, :publication_id)
    """)
    fun insert(
        @Param("title") title: String,
        @Param("publication_id") publicationId: Int,
    ): Int
    @Modifying
    @Query("UPDATE publication_section SET title = :title WHERE id = :id")
    fun updateTitle(
        @Param("id") id: Int,
        @Param("title") title: String,
    ): Int
    @Modifying
    @Query("UPDATE publication_section SET description = :description WHERE id = :id")
    fun updateDescription(
        @Param("id") id: Int,
        @Param("description") description: String,
    ): Int
    @Query("SELECT EXISTS (SELECT 1 FROM publication_section WHERE id = :id)")
    fun exists(@Param("id") id: Int): Boolean

    @Modifying
    @Query("UPDATE publication_section SET is_hidden = :is_hidden WHERE id = :id")
    fun updateHidden(@Param("id") id: Int, @Param("is_hidden") isHidden: Boolean): Int

    @Modifying
    @Query("DELETE FROM publication_section WHERE id = :id")
    fun delete(@Param("id") id: Int): Int
}