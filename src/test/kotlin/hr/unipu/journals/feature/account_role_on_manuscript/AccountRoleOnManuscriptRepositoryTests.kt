package hr.unipu.journals.feature.account_role_on_manuscript

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import kotlin.test.Test

@DataJdbcTest
class AccountRoleOnManuscriptRepositoryTests {
    @Autowired
    private lateinit var accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository

    //@Test fun `retrieve authors by manuscript id`() {}
}