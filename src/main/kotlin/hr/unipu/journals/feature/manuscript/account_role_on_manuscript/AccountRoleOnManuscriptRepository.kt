package hr.unipu.journals.feature.manuscript.account_role_on_manuscript

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface AccountRoleOnManuscriptRepository : Repository<AccountRoleOnManuscript, Int> {
    @Query("""
        SELECT DISTINCT * FROM account_role_on_manuscript
        WHERE (manuscript_id = :manuscript_id OR :manuscript_id IS NULL)
        AND (account_id = :account_id OR :account_id IS NULL)
        AND (account_role = :role::manuscript_role OR :role IS NULL)
    """)
    fun all(
        @Param("manuscript_id") manuscriptId: Int? = null,
        @Param("account_id") accountId: Int? = null,
        @Param("role") role: ManuscriptRole? = null,
    ): List<AccountRoleOnManuscript>

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

    @Query("SELECT * FROM account_role_on_manuscript WHERE account_id = :account_id")
    fun allAffiliatedRolesAndManuscriptIds(@Param("account_id") accountId: Int): List<AccountRoleOnManuscript>

    @Modifying
    @Query("INSERT INTO account_role_on_manuscript (manuscript_id, account_id, account_role) VALUES (:manuscript_id, :account_id, :account_role::manuscript_role)")
    fun assign(
        @Param("account_role") accountRole: ManuscriptRole,
        @Param("account_id") accountId: Int,
        @Param("manuscript_id") manuscriptId: Int,
    )

    @Modifying
    @Query("""
        DELETE FROM account_role_on_manuscript
        WHERE (manuscript_id = :manuscript_id OR :manuscript_id IS NULL)
        AND account_id = :account_id
        AND (account_role = :account_role::manuscript_role OR :account_role IS NULL)
        """)
    fun revoke(
        @Param("manuscript_id") manuscriptId: Int? = null,
        @Param("account_id") accountId: Int,
        @Param("account_role") accountRole: ManuscriptRole? = null
    ): Int
}

