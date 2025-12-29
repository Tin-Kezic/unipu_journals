package hr.unipu.journals.feature.manuscript.core

import hr.unipu.journals.feature.publication.core.Role
import hr.unipu.journals.feature.publication.core.Sorting
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface ManuscriptRepository: Repository<Manuscript, Int> {
    @Query("""
        SELECT
            COALESCE(
                array_agg(DISTINCT account_role_on_manuscript.account_role) FILTER (WHERE account_role_on_manuscript.account_id = :account_id),
            '{}') AS roles,
            manuscript.*
        FROM manuscript
        JOIN publication_section ON manuscript.section_id = publication_section.id
        JOIN publication ON publication.id = publication_section.publication_id
        JOIN category ON manuscript.category_id = category.id
        LEFT JOIN eic_on_publication ON publication.id = eic_on_publication.publication_id
        LEFT JOIN section_editor_on_section ON publication_section.id = section_editor_on_section.publication_section_id AND :role IS NOT NULL
        LEFT JOIN account_role_on_manuscript ON manuscript.id = account_role_on_manuscript.manuscript_id
        LEFT JOIN account ON :account_id = account.id
        WHERE (category.name = :category OR :category IS NULL)
        AND (publication_section.id = :section_id OR :section_id IS NULL)
        AND (
            :manuscript_state_filter IS NULL
            OR
            :manuscript_state_filter = 'HIDDEN' AND (
                publication.is_hidden = TRUE
                OR publication_section.is_hidden = TRUE
                OR manuscript.current_state = 'HIDDEN'
            )
            OR publication.is_hidden = FALSE AND publication_section.is_hidden = FALSE AND (
                :manuscript_state_filter = 'PUBLISHED' AND manuscript.current_state = 'PUBLISHED' AND (
                    EXISTS (SELECT 1 FROM publication_section ps WHERE ps.publication_id = publication.id AND ps.is_hidden = FALSE)
                    OR eic_on_publication.eic_id = :account_id OR account.is_admin
                )
                OR
                :manuscript_state_filter = 'ARCHIVED' AND manuscript.current_state = 'ARCHIVED'
                OR account_role_on_manuscript.account_id = :account_id AND (
                    :manuscript_state_filter IN ('MINOR_MAJOR', 'REJECTED') AND manuscript.current_state IN ('MINOR', 'MAJOR', 'REJECTED')
                    OR
                    :manuscript_state_filter = 'ALL_AWAITING_REVIEW' AND (
                        account_role_on_manuscript.account_role IN ('EIC', 'AUTHOR') AND manuscript.current_state IN ('AWAITING_EIC_REVIEW', 'AWAITING_EDITOR_REVIEW', 'AWAITING_REVIEWER_REVIEW')
                        OR
                        account_role_on_manuscript.account_role IN ('EDITOR', 'AUTHOR') AND manuscript.current_state IN ('AWAITING_EDITOR_REVIEW', 'AWAITING_REVIEWER_REVIEW')
                        OR
                        account_role_on_manuscript.account_role IN ('REVIEWER', 'AUTHOR') AND manuscript.current_state = 'AWAITING_REVIEWER_REVIEW'
                    )
                    OR :manuscript_state_filter = 'AWAITING_EIC_REVIEW'
                        AND manuscript.current_state = 'AWAITING_EIC_REVIEW'
                        AND account_role_on_manuscript.account_role IN ('EIC', 'AUTHOR')
                    OR :manuscript_state_filter = 'AWAITING_EDITOR_REVIEW'
                        AND manuscript.current_state = 'AWAITING_EDITOR_REVIEW'
                        AND account_role_on_manuscript.account_role IN ('EIC', 'EDITOR', 'AUTHOR')
                    OR :manuscript_state_filter = 'AWAITING_REVIEWER_REVIEW'
                        AND manuscript.current_state = 'AWAITING_REVIEWER_REVIEW'
                        AND account_role_on_manuscript.account_role IN ('EIC', 'EDITOR', 'REVIEWER', 'AUTHOR')
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
                    :role = 'AUTHOR' AND account_role_on_manuscript.account_role = 'AUTHOR'
                )
            )
        )
        GROUP BY manuscript.id
        ORDER BY
            CASE WHEN :sorting = 'ALPHABETICAL_A_Z' THEN manuscript.title END,
            CASE WHEN :sorting = 'ALPHABETICAL_Z_A' THEN manuscript.title END DESC,
            CASE WHEN :sorting = 'NEWEST' THEN COALESCE(MAX(manuscript.publication_date), MAX(manuscript.submission_date)) END DESC,
            CASE WHEN :sorting = 'OLDEST' THEN COALESCE(MIN(manuscript.publication_date), MIN(manuscript.submission_date)) END
    """)
    fun all(
        @Param("section_id") sectionId: Int? = null,
        @Param("manuscript_state_filter") manuscriptStateFilter: ManuscriptStateFilter? = null,
        @Param("role") role: Role? = null,
        @Param("account_id") accountId: Int? = null,
        @Param("category") category: String? = null,
        @Param("sorting") sorting: Sorting
    ): List<AccountRolesAndManuscript>

    @Modifying
    @Query("INSERT INTO manuscript (title, category_id, section_id, file_url) VALUES (:title, :category_id, :section_id, :file_url)")
    fun insert(
        @Param("title") title: String,
        @Param("category_id") categoryId: Int,
        @Param("section_id") sectionId: Int,
        @Param("file_url") fileUrl: String
    ): Int

    @Query("SELECT EXISTS (SELECT 1 FROM manuscript WHERE id = :id)")
    fun exists(@Param("id") id: Int): Boolean

    @Modifying
    @Query("UPDATE manuscript SET current_state = :state::manuscript_state WHERE id = :id")
    fun updateState(@Param("id") id: Int, @Param("state") manuscriptState: ManuscriptState): Int

    @Modifying
    @Query("DELETE FROM manuscript WHERE id = :id")
    fun delete(@Param("id") id: Int)

    @Query("SELECT * FROM manuscript WHERE id = :id")
    fun byId(@Param("id") manuscriptId: Int): Manuscript?
}