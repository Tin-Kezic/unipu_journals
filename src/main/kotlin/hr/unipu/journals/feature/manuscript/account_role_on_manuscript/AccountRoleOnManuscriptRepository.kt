package hr.unipu.journals.feature.manuscript.account_role_on_manuscript

import hr.unipu.journals.feature.account.Account
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface AccountRoleOnManuscriptRepository : Repository<AccountRoleOnManuscript, Int> {
    @Query("""
        SELECT DISTINCT account.* FROM account_role_on_manuscript
        JOIN account ON account.id = account_role_on_manuscript.account_id
        JOIN manuscript ON manuscript.id = account_role_on_manuscript.manuscript_id
        WHERE account_role_on_manuscript.account_role = 'AUTHOR'
        AND manuscript.id = :manuscript_id
    """)
    fun authors(@Param("manuscript_id") manuscriptId: Int): List<Account>

    @Query("""
        SELECT DISTINCT account.email FROM account_role_on_manuscript
        JOIN manuscript ON manuscript.id = account_role_on_manuscript.manuscript_id
        JOIN publication_section ON publication_section.id = manuscript.section_id
        JOIN publication ON publication.id = publication_section.publication_id
        JOIN account ON account.id = account_role_on_manuscript.account_id
        WHERE publication.title = :title AND account_role_on_manuscript.account_role = 'EIC'
    """)
    fun allEicOnPublicationEmailsByPublicationTitle(@Param("title") title: String): List<String>

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
    @Query("DELETE FROM account_role_on_manuscript WHERE account_id = :account_id")
    fun revoke(@Param("account_id") accountId: Int): Int
}

