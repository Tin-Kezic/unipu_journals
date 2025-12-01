package hr.unipu.journals.feature.manuscript.account_role_on_manuscript

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface AccountRoleOnManuscriptRepository : Repository<AccountRoleOnManuscript, Int> {
    @Query("""
        SELECT DISTINCT account.full_name FROM account_role_on_manuscript
        JOIN account ON account.id = account_role_on_manuscript.account_id
        JOIN manuscript ON manuscript.id = account_role_on_manuscript.manuscript_id
        WHERE account_role_on_manuscript.account_role IN ('CORRESPONDING_AUTHOR', 'AUTHOR')
        AND manuscript.id = :manuscript_id
    """)
    fun authors(@Param("manuscript_id") manuscriptId: Int): List<String>
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
}

