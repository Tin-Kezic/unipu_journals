package hr.unipu.journals.feature.publication.core

import hr.unipu.journals.feature.manuscript.core.ManuscriptStateFilter
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
        LEFT JOIN eic_on_publication ON publication.id = eic_on_publication.publication_id AND :affiliation IS NOT NULL
        LEFT JOIN section_editor_on_section ON publication_section.id = section_editor_on_section.publication_section_id AND :affiliation IS NOT NULL
        LEFT JOIN account_role_on_manuscript ON manuscript.id = account_role_on_manuscript.manuscript_id
        LEFT JOIN account ON :account_id = account.id
        WHERE (category.name = :category OR :category IS NULL)
        AND (
            :manuscript_state_filter = 'HIDDEN' AND (
                publication.is_hidden = TRUE
                OR publication_section.is_hidden = TRUE
                OR manuscript.current_state = 'HIDDEN'
            )
            OR publication.is_hidden = FALSE AND (
                :manuscript_state_filter = 'PUBLISHED' AND (
                    EXISTS (SELECT 1 FROM publication_section ps WHERE ps.publication_id = publication.id AND ps.is_hidden = FALSE)
                    OR eic_on_publication.eic_id = :account_id OR account.is_admin
                )
                OR
                publication_section.is_hidden = FALSE AND (
                    :manuscript_state_filter = 'ARCHIVED' AND manuscript.current_state = 'ARCHIVED'
                    OR
                    account_role_on_manuscript.account_id = :account_id AND (
                        :manuscript_state_filter IN ('MINOR_MAJOR', 'REJECTED') AND manuscript.current_state IN ('MINOR', 'MAJOR', 'REJECTED')
                        OR
                        :manuscript_state_filter = 'ALL_AWAITING_REVIEW' AND (
                            account_role_on_manuscript.account_role = 'EIC'
                            AND manuscript.current_state IN ('AWAITING_EIC_REVIEW', 'AWAITING_EDITOR_REVIEW', 'AWAITING_REVIEWER_REVIEW')
                            OR
                            account_role_on_manuscript.account_role = 'EDITOR'
                            AND manuscript.current_state IN ('AWAITING_EDITOR_REVIEW', 'AWAITING_REVIEWER_REVIEW')
                            OR
                            account_role_on_manuscript.account_role = 'REVIEWER'
                            AND manuscript.current_state = 'AWAITING_REVIEWER_REVIEW'
                        )
                        OR
                        :manuscript_state_filter = 'AWAITING_EIC_REVIEW' AND (
                            manuscript.current_state = 'AWAITING_EIC_REVIEW'
                            AND account_role_on_manuscript.account_role = 'EIC'
                        )
                        OR
                        :manuscript_state_filter = 'AWAITING_EDITOR_REVIEW' AND (
                            manuscript.current_state = 'AWAITING_EDITOR_REVIEW'
                            AND account_role_on_manuscript.account_role IN ('EIC', 'EDITOR')
                        )
                        OR
                        :manuscript_state_filter = 'AWAITING_REVIEWER_REVIEW' AND (
                            manuscript.current_state = 'AWAITING_REVIEWER_REVIEW'
                            AND account_role_on_manuscript.account_role IN ('EIC', 'EDITOR', 'REVIEWER')
                        )
                    )
                )
            )
        ) AND (
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
        ORDER BY publication.title
    """)
    fun all(
        @Param("manuscript_state_filter") manuscriptStateFilter: ManuscriptStateFilter,
        @Param("affiliation") affiliation: Affiliation? = null,
        @Param("account_id") accountId: Int? = null,
        @Param("category") category: String? = null,
        @Param("sorting") sorting: Sorting? = null
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