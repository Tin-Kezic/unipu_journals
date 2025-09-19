package hr.unipu.journals.data_jdbc_tests

import hr.unipu.journals.feature.account.Account
import hr.unipu.journals.feature.account.AccountRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import kotlin.test.Test
import kotlin.test.assertEquals

@DataJdbcTest
class AccountRepositoryTests {
    @Autowired private lateinit var accountRepository: AccountRepository
    @Autowired private lateinit var jdbcTemplate: JdbcTemplate
    @Test fun `insert account`() {
        accountRepository.insert(
            fullName = "John",
            title = "Dr.",
            email = "john@unipu.hr",
            password = "secure123",
            affiliation = "University",
            jobType = "Professor",
            country = "USA",
            city = "Cheyenne",
            address = "200 West 24th Street",
            zipCode = "82002-0020"
        )
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM account WHERE email = 'john@unipu.hr')"))
    }
    @Test fun `assign and revoke admin, check is admin`() {
        accountRepository.updateIsAdmin("new.admin@unipu.hr", true)
        assertTrue(accountRepository.isAdmin("new.admin@unipu.hr"))
    }
    @Test fun `revoke admin`() {
        accountRepository.updateIsAdmin("revoked.admin@unipu.hr", false)
        assertFalse(accountRepository.isAdmin("revoked.admin@unipu.hr"))
        assertTrue(accountRepository.isAdmin("admin1@unipu.hr"))
    }
    @Test fun `update root account password`() {
        accountRepository.updateRootPassword("newPassword")
        assertEquals("newPassword", accountRepository.byEmail("root@unipu.hr")?.password)
    }
    @Test fun `account exists by email`() = assertTrue(accountRepository.emailExists("root@unipu.hr"))
    @Test fun `retrieve account by id`() = assertEquals("root@unipu.hr", accountRepository.byId(1)?.email)
    @Test fun `retrieve account by email`() = assertEquals(1, accountRepository.byEmail("root@unipu.hr")?.id)
    @Test fun `account exists by id`() {
        assertTrue(accountRepository.exists(1))
        assertFalse(accountRepository.exists(100))
    }
    @Test fun `retrieve all admin emails`() {
        assertTrue(accountRepository.allAdminEmails().containsAll(listOf("admin1@unipu.hr", "admin2@unipu.hr", "revoke.admin@unipu.hr")))
        assertFalse(accountRepository.allAdminEmails().containsAll(listOf("user1@unipu.hr", "user2@unipu.hr", "user3@unipu.hr")))
    }
    @Test fun `update account`() {
        val newAccount = Account(
            id = 1,
            fullName = "new full name",
            title = "new title",
            email = "new@email",
            password = "new password",
            affiliation = "new affiliation",
            jobType = "new job type",
            country = "new country",
            city = "new city",
            address = "new address",
            zipCode = "new zip code",
            isAdmin = false
        )
        accountRepository.update(
            id = 1,
            fullName = newAccount.fullName,
            title = newAccount.title,
            email = newAccount.email,
            password = newAccount.password,
            affiliation = newAccount.affiliation,
            jobType = newAccount.jobType,
            country = newAccount.country,
            city = newAccount.city,
            address = newAccount.address,
            zipCode = newAccount.zipCode,
        )
        assertEquals(newAccount, accountRepository.byEmail(newAccount.email))
    }
    @Test fun `delete account`() {
        accountRepository.delete(1)
        assertNull(accountRepository.byId(1))
        assertNotNull(accountRepository.byId(2))
    }
}