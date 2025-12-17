package hr.unipu.journals.feature.manuscript.account_role_on_manuscript

import hr.unipu.journals.feature.manuscript.core.AffiliatedManuscript
import hr.unipu.journals.feature.manuscript.core.ManuscriptStateFilter
import hr.unipu.journals.feature.publication.core.Sorting
import org.springframework.data.domain.Sort
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface AccountRoleOnManuscriptRepository : Repository<AccountRoleOnManuscript, Int> {
    @Query("SELECT account_role FROM account_role_on_manuscript WHERE account_id = :account_id AND manuscript_id = :manuscript_id")
    fun role(@Param("account_id") accountId: Int, @Param("manuscript_id") manuscriptId: Int): List<ManuscriptRole>

    @Query("""
        SELECT DISTINCT account.full_name FROM account_role_on_manuscript
        JOIN account ON account.id = account_role_on_manuscript.account_id
        JOIN manuscript ON manuscript.id = account_role_on_manuscript.manuscript_id
        WHERE account_role_on_manuscript.account_role IN ('CORRESPONDING_AUTHOR', 'AUTHOR')
        AND manuscript.id = :manuscript_id
    """)
    fun authors(@Param("manuscript_id") manuscriptId: Int): List<String>

    @Query("""
        SELECT account_role_on_manuscript.account_role, manuscript.* FROM account_role_on_manuscript
        JOIN manuscript ON account_role_on_manuscript.manuscript_id = manuscript.id
        JOIN publication_section on manuscript.section_id = publication_section.id
        JOIN publication on publication_section.publication_id = publication.id
        WHERE publication_section.is_hidden = FALSE AND publication.is_hidden = FALSE
        AND publication_section.id = :section_id
        AND account_role_on_manuscript.account_id = :account_id
        AND (
            :manuscript_state_filter = 'ALL_AWAITING_REVIEW' AND (
                account_role_on_manuscript.account_role = 'EIC' AND manuscript.current_state IN ('AWAITING_EIC_REVIEW', 'AWAITING_EDITOR_REVIEW', 'AWAITING_REVIEWER_REVIEW')
                OR
                account_role_on_manuscript.account_role = 'EDITOR' AND manuscript.current_state IN ('AWAITING_EDITOR_REVIEW', 'AWAITING_REVIEWER_REVIEW')
                OR
                account_role_on_manuscript.account_role = 'REVIEWER' AND manuscript.current_state = 'AWAITING_REVIEWER_REVIEW'
            )
            OR :manuscript_state_filter = 'AWAITING_EIC_REVIEW'
                AND manuscript.current_state = 'AWAITING_EIC_REVIEW'
                AND account_role_on_manuscript.account_role = 'EIC'
            OR :manuscript_state_filter = 'AWAITING_EDITOR_REVIEW'
                AND manuscript.current_state = 'AWAITING_EDITOR_REVIEW'
                AND account_role_on_manuscript.account_role IN ('EIC', 'EDITOR')
            OR :manuscript_state_filter = 'AWAITING_REVIEWER_REVIEW'
                AND manuscript.current_state = 'AWAITING_REVIEWER_REVIEW'
                AND account_role_on_manuscript.account_role IN ('EIC', 'EDITOR', 'REVIEWER')
        )
        ORDER BY
            CASE WHEN :sorting = 'ALPHABETICAL_A_Z' THEN manuscript.title END,
            CASE WHEN :sorting = 'ALPHABETICAL_Z_A' THEN manuscript.title END DESC,
            CASE WHEN :sorting = 'NEWEST' THEN COALESCE(manuscript.publication_date, manuscript.submission_date) END DESC,
            CASE WHEN :sorting = 'OLDEST' THEN COALESCE(manuscript.publication_date, manuscript.submission_date) END
    """)
    fun affiliatedManuscripts(
        @Param("account_id") id: Int,
        @Param("manuscript_state_filter") manuscriptStateFilter: ManuscriptStateFilter,
        @Param("section_id") sectionId: Int,
        @Param("sorting") sorting: Sorting
    ): List<AffiliatedManuscript>

    @Query("""
        SELECT DISTINCT account.full_name FROM account_role_on_manuscript
        JOIN account ON account.id = account_role_on_manuscript.account_id
        JOIN manuscript ON manuscript.id = account_role_on_manuscript.manuscript_id
        WHERE manuscript.id = :manuscript_id
        AND account_role_on_manuscript.account_role = 'CORRESPONDING_AUTHOR'
    """)
    fun correspondingAuthor(@Param("manuscript_id") manuscriptId: Int): String

    @Query("""
        SELECT EXISTS (SELECT 1 FROM account_role_on_manuscript
        WHERE account_role_on_manuscript.account_role = :account_role::manuscript_role
        AND account_role_on_manuscript.account_id = :account_id
        AND account_role_on_manuscript.manuscript_id = :manuscript_id
        )
    """)
    fun isRoleOnManuscript(
        @Param("account_role") accountRole: ManuscriptRole,
        @Param("account_id") accountId: Int,
        @Param("manuscript_id") manuscriptId: Int,
    ): Boolean

    @Modifying
    @Query("INSERT INTO account_role_on_manuscript (manuscript_id, account_id, account_role) VALUES (:manuscript_id, :account_id, :account_role::manuscript_role)")
    fun assign(
        @Param("account_role") accountRole: ManuscriptRole,
        @Param("account_id") accountId: Int,
        @Param("manuscript_id") manuscriptId: Int,
    )
}

