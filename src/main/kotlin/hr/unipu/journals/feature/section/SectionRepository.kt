package hr.unipu.journals.feature.section

import hr.unipu.journals.feature.manuscript.ManuscriptState
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param
import java.util.Optional

interface SectionRepository: Repository<Section, Int> {
    @Query("SELECT * from publication_section WHERE id = :id")
    fun byId(@Param("id") sectionId: Int): Section

    @Query("""
        SELECT DISTINCT publication_section.title FROM publication_section
        JOIN publication ON publication_section.publication_id = publication.id
        WHERE publication.title = :title
        AND publication.is_hidden = FALSE
        AND publication_section.is_hidden = FALSE
        """)
    fun allPublishedTitlesByPublicationTitle(@Param("title") publicationTitle: String): List<String>

    @Query("""
        SELECT DISTINCT publication_section.* FROM publication_section
        JOIN publication ON publication_section.publication_id = publication.id
        JOIN manuscript ON manuscript.section_id = publication_section.id
        WHERE publication.is_hidden = FALSE
        AND publication_section.is_hidden = FALSE
        AND manuscript.current_state = 'PUBLISHED'
        ORDER BY publication_section.id DESC
    """)
    fun allPublishedByPublicationId(@Param("publication_id") publicationId: Int, @Param("state") manuscriptState: ManuscriptState? = null): List<Section>

    @Query("""
        SELECT DISTINCT publication_section.* FROM publication_section
        JOIN manuscript ON manuscript.section_id = publication_section.id
        WHERE publication_section.publication_id = :publication_id
        AND (
            :state IS NULL
            AND (publication_section.is_hidden = FALSE OR manuscript.current_state = 'PUBLISHED')
            OR (
                :state = 'HIDDEN'
                AND (publication_section.is_hidden = TRUE OR manuscript.current_state = 'HIDDEN')
            )
        )
        ORDER BY publication_section.id DESC
    """)
    fun allArchivedByPublicationId(@Param("publication_id") publicationId: Int, @Param("state") manuscriptState: ManuscriptState? = null): List<Section>

    @Query("""
        SELECT DISTINCT publication_section.* FROM publication_section
        JOIN manuscript ON manuscript.section_id = publication_section.id
        WHERE publication_section.publication_id = :publication_id
        AND (
            :state IS NULL
            AND (publication_section.is_hidden = FALSE OR manuscript.current_state = 'PUBLISHED')
            OR (
                :state = 'HIDDEN'
                AND (publication_section.is_hidden = TRUE OR manuscript.current_state = 'HIDDEN')
            )
        )
        ORDER BY publication_section.id DESC
    """)
    fun allHiddenByPublicationId(@Param("publication_id") publicationId: Int, @Param("state") manuscriptState: ManuscriptState? = null): List<Section>

    @Query("""
        SELECT DISTINCT publication_section.* FROM publication_section
        JOIN publication ON publication_section.publication_id = publication.id
        LEFT JOIN manuscript ON manuscript.section_id = publication_section.id
        WHERE publication.id = :publication_id
        AND ((
            publication.is_hidden = FALSE
            AND publication_section.is_hidden = FALSE
            AND (:state IS NULL OR (:state = 'ARCHIVED' AND manuscript.current_state = 'ARCHIVED'))
        ) OR (
            :state = 'HIDDEN'
            AND (
                publication.is_hidden = TRUE
                OR publication_section.is_hidden = TRUE
                OR manuscript.current_state = 'HIDDEN'
            )
        ))
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
    @Query("""
        UPDATE publication_section SET
        title = COALESCE(:title, title),
        description = COALESCE(:description, description),
        is_hidden = COALESCE(:is_hidden, is_hidden)
        WHERE id = :id
        """)
    fun update(
        @Param("id") id: Int,
        @Param("title") title: String? = null,
        @Param("description") description: String? = null,
        @Param("is_hidden") isHidden: Boolean? = null
    ): Int

    @Query("SELECT EXISTS (SELECT 1 FROM publication_section WHERE id = :id)")
    fun exists(@Param("id") id: Int): Boolean

    @Modifying
    @Query("DELETE FROM publication_section WHERE id = :id")
    fun delete(@Param("id") id: Int): Int
}