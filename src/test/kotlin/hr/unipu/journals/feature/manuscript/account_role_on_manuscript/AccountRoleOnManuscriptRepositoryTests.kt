package hr.unipu.journals.feature.manuscript.account_role_on_manuscript

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest

@DataJdbcTest
class AccountRoleOnManuscriptRepositoryTests {
    @Autowired private lateinit var accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository

    @Test fun `retrieve authors by manuscript id`() {
        assertEquals(
            listOf("author on manuscript1", "corresponding author on manuscript1"),
            accountRoleOnManuscriptRepository.authors(1)
        )
        assertEquals(
            listOf("author on manuscript2", "corresponding author on manuscript2"),
            accountRoleOnManuscriptRepository.authors(2)
        )
    }
    @Test fun `retrieve corresponding author by manuscript id`() {
        assertEquals("corresponding author on manuscript1", accountRoleOnManuscriptRepository.correspondingAuthor(1))
        assertEquals("corresponding author on manuscript2", accountRoleOnManuscriptRepository.correspondingAuthor(2))
    }
    @Test fun `assert is role on manuscript`() {
        Assertions.assertTrue(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EIC, 11, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EIC, 11, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EDITOR, 11, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.REVIEWER, 11, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.CORRESPONDING_AUTHOR, 11, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.AUTHOR, 11, 1))

        Assertions.assertTrue(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EIC, 12, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EIC, 12, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EDITOR, 12, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.REVIEWER, 12, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.CORRESPONDING_AUTHOR, 12, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.AUTHOR, 12, 2))

        Assertions.assertTrue(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EDITOR, 13, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EDITOR, 13, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EIC, 13, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.REVIEWER, 13, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.CORRESPONDING_AUTHOR, 13, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.AUTHOR, 13, 1))

        Assertions.assertTrue(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EDITOR, 14, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EDITOR, 14, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EIC, 14, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.REVIEWER, 14, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.CORRESPONDING_AUTHOR, 14, 2 ))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.AUTHOR, 14, 2))

        Assertions.assertTrue(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.REVIEWER, 15, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.REVIEWER, 15, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EIC, 15, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EDITOR, 15, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.CORRESPONDING_AUTHOR, 15, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.AUTHOR, 15, 1))

        Assertions.assertTrue(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.REVIEWER, 16, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.REVIEWER, 16, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EIC, 16, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EDITOR, 16, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.CORRESPONDING_AUTHOR, 16, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.AUTHOR, 16, 2))

        Assertions.assertTrue(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.CORRESPONDING_AUTHOR, 17, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.CORRESPONDING_AUTHOR, 17, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EIC, 17, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EDITOR, 17, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.REVIEWER, 17, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.AUTHOR, 17, 1))

        Assertions.assertTrue(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.CORRESPONDING_AUTHOR, 18, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.CORRESPONDING_AUTHOR, 18, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EIC, 18, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EDITOR, 18, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.REVIEWER, 18, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.AUTHOR, 18, 2))

        Assertions.assertTrue(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.AUTHOR, 19, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.AUTHOR, 19, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EIC, 19, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EDITOR, 19, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.REVIEWER, 19, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.CORRESPONDING_AUTHOR, 19, 1))

        Assertions.assertTrue(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.AUTHOR, 20, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.AUTHOR, 20, 1))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EIC, 20, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.EDITOR, 20, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.REVIEWER, 20, 2))
        Assertions.assertFalse(accountRoleOnManuscriptRepository.isRoleOnManuscript(ManuscriptRole.CORRESPONDING_AUTHOR, 20, 2))
    }
}