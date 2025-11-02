package hr.unipu.journals.feature.publication.core

import hr.unipu.journals.feature.manuscript.category.Category
import hr.unipu.journals.feature.manuscript.core.ManuscriptState
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface PublicationRepository: Repository<Publication, Int> {
    @Query("SELECT title FROM publication WHERE id = :id")
    fun title(@Param("id") id: Int): String

    @Query("""
        SELECT DISTINCT publication.* FROM publication
        LEFT JOIN publication_section ON publication.id = publication_section.publication_id
        LEFT JOIN manuscript ON publication_section.id = manuscript.section_id
        LEFT JOIN category ON manuscript.category_id = category.id
        LEFT JOIN eic_on_publication ON publication.id = eic_on_publication.publication_id
        LEFT JOIN section_editor_on_section ON publication_section.id = section_editor_on_section.publication_section_id
        LEFT JOIN account_role_on_manuscript ON manuscript.id = account_role_on_manuscript.manuscript_id
        WHERE (manuscript.current_state = :manuscript_state OR :manuscript_state IS NULL)
        AND (account_role_on_manuscript.account_id = :account_id OR :account_id IS NULL)
        AND (category.name = :category OR :category IS NULL)
        AND (
            :affiliation IS NULL
            OR (
                :affiliation = 'EIC_ON_PUBLICATION' AND eic_on_publication.eic_id = :account_id
                OR
                :affiliation = 'SECTION_EDITOR' AND section_editor_on_section.section_editor_id = :account_id
                OR
                account_role_on_manuscript.account_id = :account_id AND (
                    :affiliation = 'EIC_ON_MANUSCRIPT' AND account_role_on_manuscript.account_role = 'EIC'
                    OR
                    :affiliation = 'EDITOR' AND account_role_on_manuscript.account_role = 'EDITOR'
                    OR
                    :affiliation = 'REVIEWER' AND account_role_on_manuscript.account_role = 'REVIEWER'
                    OR
                    :affiliation = 'CORRESPONDING_AUTHOR' AND account_role_on_manuscript.account_role = 'CORRESPONDING_AUTHOR'
                    OR
                    :affiliation = 'AUTHOR' AND account_role_on_manuscript.account_role = 'AUTHOR'
                )
            )
        )
        AND (
            :publication_type = 'HIDDEN' AND (
                publication.is_hidden = TRUE
                OR publication_section.is_hidden = TRUE
                OR manuscript.current_state = 'HIDDEN'
            )
            OR
            publication.is_hidden = FALSE AND (
                :publication_type = 'PUBLIC'
                OR
                publication_section.is_hidden = FALSE AND (
                    :publication_type = 'CONTAINS_ARCHIVED_MANUSCRIPTS'
                    AND manuscript.current_state = 'ARCHIVED'
                    OR
                    :publication_type = 'CONTAINS_PENDING_MANUSCRIPTS'
                    AND (
                        account_role_on_manuscript.account_role = 'EIC'
                        AND manuscript.current_state IN ('AWAITING_EIC_REVIEW', 'AWAITING_EDITOR_REVIEW', 'AWAITING_REVIEWER_REVIEW')
                        OR
                        account_role_on_manuscript.account_role = 'EDITOR'
                        AND manuscript.current_state IN ('AWAITING_EDITOR_REVIEW', 'AWAITING_REVIEWER_REVIEW')
                        OR
                        account_role_on_manuscript.account_role = 'REVIEWER'
                        AND manuscript.current_state = 'AWAITING_REVIEWER_REVIEW'
                    )
                )
            )
        )
        ORDER BY publication.title
    """)
    fun all(
        @Param("publication_type") publicationType: PublicationType,
        @Param("affiliation") affiliation: Affiliation? = null,
        @Param("account_id") accountId: Int? = null,
        @Param("manuscript_state") manuscriptState: ManuscriptState? = null,
        @Param("category") category: String? = null
    ): List<Publication>

    @Modifying
    @Query("INSERT INTO publication (title) VALUES (:title)")
    fun insert(@Param("title") title: String): Int

    @Modifying
    @Query("""
        UPDATE publication SET
        title = COALESCE(:title, title),
        is_hidden = COALESCE(:is_hidden, is_hidden)
        WHERE id = :id
    """)
    fun update(
        @Param("id") id: Int,
        @Param("title") title: String? = null,
        @Param("is_hidden") isHidden: Boolean? = null,
    ): Int

    @Query("SELECT EXISTS (SELECT 1 FROM publication WHERE id = :id)")
    fun exists(@Param("id") id: Int): Boolean

    @Modifying
    @Query("DELETE FROM publication WHERE id = :id")
    fun delete(@Param("id") id: Int): Int
}