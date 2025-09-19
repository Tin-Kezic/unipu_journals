package hr.unipu.journals.feature.account_role_on_manuscript

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import kotlin.test.Test
import kotlin.test.assertContains

@DataJdbcTest
class AccountRoleOnManuscriptRepositoryTests {
    @Autowired
    private lateinit var accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository

    @Test fun `retrieve authors by manuscript id`() {
        val authorsOnManuscript1 = accountRoleOnManuscriptRepository.authors(1)
        val authorsOnManuscript2 = accountRoleOnManuscriptRepository.authors(2)

        assertContains(authorsOnManuscript1, "first author")
        assertContains(authorsOnManuscript2, "second author")

        assertFalse(authorsOnManuscript1.contains("second author"))
        assertFalse(authorsOnManuscript2.contains("first author"))
    }
}