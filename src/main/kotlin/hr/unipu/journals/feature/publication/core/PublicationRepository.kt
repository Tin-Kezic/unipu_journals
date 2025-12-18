package hr.unipu.journals.feature.publication.core

import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.ManuscriptRole
import hr.unipu.journals.feature.manuscript.core.ManuscriptStateFilter
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface PublicationRepository: Repository<Publication, Int> {
    @Query("SELECT title FROM publication WHERE id = :id")
    fun title(@Param("id") id: Int): String

    @Query("""
        SELECT publication.* FROM publication
        LEFT JOIN publication_section ON publication.id = publication_section.publication_id
        LEFT JOIN manuscript ON publication_section.id = manuscript.section_id
        LEFT JOIN category ON manuscript.category_id = category.id
        LEFT JOIN eic_on_publication ON publication.id = eic_on_publication.publication_id
        LEFT JOIN section_editor_on_section ON publication_section.id = section_editor_on_section.publication_section_id
        LEFT JOIN account_role_on_manuscript ON manuscript.id = account_role_on_manuscript.manuscript_id
        LEFT JOIN invite on invite.target_id = manuscript.id
        WHERE (:category IS NULL OR category.name = :category)
        AND (
            :manuscript_state_filter = 'HIDDEN' AND (
                publication.is_hidden = TRUE
                OR publication_section.is_hidden = TRUE
                OR manuscript.current_state = 'HIDDEN'
            )
            OR publication.is_hidden = FALSE AND (
                :manuscript_state_filter = 'PUBLISHED' AND (
                    publication_section.is_hidden = FALSE AND manuscript.current_state = :manuscript_state_filter::manuscript_state
                    OR account.is_admin
                    OR eic_on_publication.eic_id = :account_id
                    OR section_editor_on_section.section_editor_id = :account_id
                )
                OR
                publication_section.is_hidden = FALSE AND (
                    :manuscript_state_filter = 'ARCHIVED' AND manuscript.current_state = 'ARCHIVED'
                    OR
                    account_role_on_manuscript.account_id = :account_id AND (
                        :manuscript_state_filter = 'MINOR_MAJOR' AND manuscript.current_state IN ('MINOR', 'MAJOR')
                        OR
                        :manuscript_state_filter = 'REJECTED' AND manuscript.current_state = 'REJECTED'
                    )
                    OR
                    :manuscript_state_filter IN ('MINOR_MAJOR', 'REJECTED') AND manuscript.current_state IN ('MINOR', 'MAJOR', 'REJECTED')
                    OR
                    :manuscript_state_filter = 'ALL_AWAITING_REVIEW' AND (
                        manuscript.current_state IN ('AWAITING_EIC_REVIEW', 'AWAITING_EDITOR_REVIEW', 'AWAITING_REVIEWER_REVIEW') AND (
                            account_role_on_manuscript.account_id = :account_id AND account_role_on_manuscript.account_role = 'EIC'
                            OR invite.target_id = manuscript.id AND invite.target = 'EIC_ON_MANUSCRIPT' AND invite.email = account.email
                        )
                        OR
                        manuscript.current_state IN ('AWAITING_EDITOR_REVIEW', 'AWAITING_REVIEWER_REVIEW') AND (
                            account_role_on_manuscript.account_id = :account_id AND account_role_on_manuscript.account_role = 'EDITOR'
                            OR invite.target_id = manuscript.id AND invite.target = 'EDITOR' AND invite.email = account.email
                        )
                        OR
                        manuscript.current_state = 'AWAITING_REVIEWER_REVIEW' AND (
                            account_role_on_manuscript.account_id = :account_id AND account_role_on_manuscript.account_role = 'REVIEWER'
                            OR invite.target_id = manuscript.id AND invite.target = 'REVIEWER' AND invite.email = account.email
                        )
                    )
                    OR :manuscript_state_filter = 'AWAITING_EIC_REVIEW' AND manuscript.current_state = 'AWAITING_EIC_REVIEW' AND (
                        account_role_on_manuscript.account_id = :account_id AND account_role_on_manuscript.account_role = 'EIC'
                        OR invite.target_id = manuscript.id AND invite.target = 'EIC_ON_MANUSCRIPT' AND invite.email = account.email
                    )
                    OR :manuscript_state_filter = 'AWAITING_EDITOR_REVIEW' AND manuscript.current_state = 'AWAITING_EDITOR_REVIEW' AND (
                        account_role_on_manuscript.account_id = :account_id AND account_role_on_manuscript.account_role IN ('EIC', 'EDITOR')
                        OR invite.target_id = manuscript.id AND invite.target IN ('EIC_ON_MANUSCRIPT', 'EDITOR') AND invite.email = account.email
                    )
                    OR :manuscript_state_filter = 'AWAITING_REVIEWER_REVIEW' AND manuscript.current_state = 'AWAITING_REVIEWER_REVIEW' AND (
                        account_role_on_manuscript.account_id = :account_id AND account_role_on_manuscript.account_role IN ('EIC', 'EDITOR', 'REVIEWER')
                        OR invite.target_id = manuscript.id AND invite.target IN ('EIC_ON_MANUSCRIPT', 'EDITOR', 'REVIEWER') AND invite.email = account.email
                    )
                )
            )
        ) AND (
            :role IS NULL
            OR (
                :role = 'EIC_ON_PUBLICATION' AND eic_on_publication.eic_id = :account_id
                OR
                :role = 'SECTION_EDITOR' AND section_editor_on_section.section_editor_id = :account_id
                OR
                account_role_on_manuscript.account_id = :account_id AND (
                    :role = 'EIC_ON_MANUSCRIPT' AND account_role_on_manuscript.account_role = 'EIC'
                    OR
                    :role = 'EDITOR' AND account_role_on_manuscript.account_role = 'EDITOR'
                    OR
                    :role = 'REVIEWER' AND account_role_on_manuscript.account_role = 'REVIEWER'
                    OR
                    :role = 'CORRESPONDING_AUTHOR' AND account_role_on_manuscript.account_role = 'CORRESPONDING_AUTHOR'
                    OR
                    :role = 'AUTHOR' AND account_role_on_manuscript.account_role = 'AUTHOR'
                )
            )
        )
        GROUP BY publication.id
        ORDER BY
            CASE WHEN :sorting = 'ALPHABETICAL_A_Z' THEN publication.title END,
            CASE WHEN :sorting = 'ALPHABETICAL_Z_A' THEN publication.title END DESC,
            CASE WHEN :sorting = 'NEWEST' THEN COALESCE(MAX(manuscript.publication_date), MAX(manuscript.submission_date)) END DESC,
            CASE WHEN :sorting = 'OLDEST' THEN COALESCE(MIN(manuscript.publication_date), MIN(manuscript.submission_date)) END
    """)
    fun all(
        @Param("manuscript_state_filter") manuscriptStateFilter: ManuscriptStateFilter,
        @Param("role") role: Role? = null,
        @Param("account_id") accountId: Int? = null,
        @Param("category") category: String? = null,
        @Param("sorting") sorting: Sorting? = Sorting.ALPHABETICAL_A_Z
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