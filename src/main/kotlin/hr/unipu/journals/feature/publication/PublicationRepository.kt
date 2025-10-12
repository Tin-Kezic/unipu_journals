package hr.unipu.journals.feature.publication

import hr.unipu.journals.feature.manuscript.ManuscriptState
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

private const val UNDER_REVIEW = "'AWAITING_EIC_REVIEW', 'AWAITING_EDITOR_REVIEW', 'AWAITING_REVIEWER_REVIEW', 'MINOR', 'MAJOR'"

interface PublicationRepository: Repository<Publication, Int> {
    @Query("""
        SELECT DISTINCT publication.* FROM publication
        JOIN publication_section ON publication.id = publication_section.publication_id
        JOIN manuscript ON publication_section.id = manuscript.section_id
        JOIN eic_on_publication ON publication.id = eic_on_publication.publication_id
        JOIN account_role_on_manuscript ON manuscript.id = account_role_on_manuscript.manuscript_id
        WHERE account_role_on_manuscript.account_id = :account_id
        AND (
            account_role_on_manuscript.target IN ('EIC_ON_MANUSCRIPT', 'EDITOR_ON_MANUSCRIPT')
            AND manuscript.current_state IN ('AWAITING_EIC_REVIEW', 'AWAITING_EDITOR_REVIEW', 'AWAITING_REVIEWER_REVIEW')
        ) OR (
            account_role_on_manuscript.target = 'REVIEWER_ON_MANUSCRIPT'
            AND manuscript.current_state = 'AWAITING_REVIEWER_REVIEW'
        )
        AND publication.is_hidden = FALSE
        AND publication_section.is_hidden = FALSE
    """)
    fun allWhichContainManuscriptsUnderReviewWithAffiliation(@Param("account_id") accountId: Int): List<Publication>

    @Query("SELECT title FROM publication WHERE id = :id")
    fun title(@Param("id") id: Int): String

    @Query("""
        SELECT DISTINCT publication.* FROM publication
        JOIN publication_section ON publication.id = publication_section.publication_id
        JOIN manuscript ON publication_section.id = manuscript.section_id
        WHERE (:state IS NULL AND publication.is_hidden = FALSE)
        OR (
            publication.is_hidden = FALSE
            AND publication_section.is_hidden = FALSE
            AND (
                manuscript.current_state = 'ARCHIVED' AND :state = 'ARCHIVED'
                OR manuscript.current_state IN ($UNDER_REVIEW) AND :state IN ($UNDER_REVIEW)
            )
        ) OR (
            publication.is_hidden = TRUE
            AND manuscript.current_state = 'HIDDEN'
            AND :state = 'HIDDEN'
        )
        ORDER BY id DESC
    """)
    fun all(@Param("state") manuscriptState: ManuscriptState? = null): List<Publication>

    @Modifying
    @Query("INSERT INTO publication (title) VALUES (:title)")
    fun insert(@Param("title") title: String)

    @Modifying
    @Query("UPDATE publication SET title = :title WHERE id = :id")
    fun updateTitle(@Param("id") id: Int, @Param("title") title: String)

    @Query("SELECT EXISTS (SELECT 1 FROM publication WHERE id = :id)")
    fun exists(@Param("id") id: Int): Boolean

    @Modifying
    @Query("UPDATE publication SET is_hidden = :is_hidden WHERE id = :id")
    fun updateHidden(@Param("id") id: Int, @Param("is_hidden") isHidden: Boolean)

    @Modifying
    @Query("DELETE FROM publication WHERE id = :id")
    fun delete(@Param("id") id: Int)
}