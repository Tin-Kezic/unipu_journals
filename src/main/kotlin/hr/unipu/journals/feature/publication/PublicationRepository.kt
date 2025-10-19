package hr.unipu.journals.feature.publication

import hr.unipu.journals.feature.manuscript.ManuscriptState
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface PublicationRepository: Repository<Publication, Int> {
    @Query("""
        SELECT DISTINCT publication.* FROM publication
        JOIN publication_section ON publication.id = publication_section.publication_id
        JOIN manuscript ON publication_section.id = manuscript.section_id
        JOIN account_role_on_manuscript ON manuscript.id = account_role_on_manuscript.manuscript_id
        WHERE account_role_on_manuscript.account_id = :account_id
        AND publication.is_hidden = FALSE
        AND publication_section.is_hidden = FALSE
        AND ((
            account_role_on_manuscript.account_role = 'EIC'
            AND manuscript.current_state IN ('AWAITING_EIC_REVIEW', 'AWAITING_EDITOR_REVIEW', 'AWAITING_REVIEWER_REVIEW')
        ) OR (
            account_role_on_manuscript.account_role = 'EDITOR'
            AND manuscript.current_state IN ('AWAITING_EDITOR_REVIEW', 'AWAITING_REVIEWER_REVIEW')
        ) OR (
            account_role_on_manuscript.account_role = 'REVIEWER'
            AND manuscript.current_state = 'AWAITING_REVIEWER_REVIEW'
        ))
    """)
    fun allWithPendingManuscripts(@Param("account_id") accountId: Int): List<Publication>

    @Query("SELECT title FROM publication WHERE id = :id")
    fun title(@Param("id") id: Int): String

    @Query("SELECT * FROM publication WHERE publication.is_hidden = FALSE")
    fun allPublished(): List<Publication>

    @Query("""
        SELECT DISTINCT publication.* FROM publication
        LEFT JOIN publication_section ON publication.id = publication_section.publication_id
        LEFT JOIN manuscript ON manuscript.section_id = publication_section.id
        WHERE publication.is_hidden = TRUE
        OR publication_section.is_hidden = TRUE
        OR manuscript.current_state = 'HIDDEN'
        """)
    fun allHidden(): List<Publication>

    @Query("""
        SELECT DISTINCT publication.* FROM publication
        JOIN publication_section ON publication.id = publication_section.publication_id
        JOIN manuscript ON manuscript.section_id = publication_section.id
        WHERE publication.is_hidden = FALSE
        AND publication_section.is_hidden = FALSE
        AND manuscript.current_state = 'ARCHIVED'
    """)
    fun allContainingArchivedManuscripts(): List<Publication>

    @Modifying
    @Query("INSERT INTO publication (title) VALUES (:title)")
    fun insert(@Param("title") title: String): Int

    @Modifying
    @Query("UPDATE publication SET title = :title WHERE id = :id")
    fun updateTitle(@Param("id") id: Int, @Param("title") title: String): Int

    @Query("SELECT EXISTS (SELECT 1 FROM publication WHERE id = :id)")
    fun exists(@Param("id") id: Int): Boolean

    @Modifying
    @Query("UPDATE publication SET is_hidden = :is_hidden WHERE id = :id")
    fun updateHidden(@Param("id") id: Int, @Param("is_hidden") isHidden: Boolean): Int

    @Modifying
    @Query("DELETE FROM publication WHERE id = :id")
    fun delete(@Param("id") id: Int): Int
}