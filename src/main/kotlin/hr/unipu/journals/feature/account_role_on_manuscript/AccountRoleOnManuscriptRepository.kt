package hr.unipu.journals.feature.account_role_on_manuscript

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

private const val ACCOUNT_ROLE_ON_MANUSCRIPT = "account_role_on_manuscript"
private const val ID = "id"
private const val MANUSCRIPT_ID = "manuscript_id"
private const val ACCOUNT_ID = "account_id"
private const val ACCOUNT_ROLE = "account_role"
interface AccountRoleOnManuscriptRepository : Repository<AccountRoleOnManuscript, Int> {
    @Query("SELECT EXISTS (SELECT 1 FROM $ACCOUNT_ROLE_ON_MANUSCRIPT WHERE $MANUSCRIPT_ID = :$MANUSCRIPT_ID AND $ACCOUNT_ID = :$ACCOUNT_ID")
    fun isEicOnManuscript(@Param(ACCOUNT_ID) eicId: Int, @Param(MANUSCRIPT_ID) manuscriptId: Int)
}

