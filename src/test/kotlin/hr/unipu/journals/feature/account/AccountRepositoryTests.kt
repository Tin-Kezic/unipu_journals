package hr.unipu.journals.feature.account

import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import kotlin.test.Test
import kotlin.test.assertEquals

@DataJdbcTest
class AccountRepositoryTests {
    @Autowired
    private lateinit var accountRepository: AccountRepository
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate
    @Test
    fun `insert account`() {
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM account WHERE email = 'john@unipu.hr')"))
        assertEquals(
            1, accountRepository.insert(
                AccountDTO(
                    fullName = "John",
                    title = "Dr.",
                    email = "john@unipu.hr",
                    password = "secure123",
                    passwordConfirmation = "secure123",
                    affiliation = "University",
                    jobType = "Professor",
                    country = "USA",
                    city = "Cheyenne",
                    address = "200 West 24th Street",
                    zipCode = "82002-0020"
                )
            )
        )
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM account WHERE email = 'john@unipu.hr')"))
    }
    @Test
    fun `assign admin`() {
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM account WHERE email = 'new.admin@unipu.hr' AND is_admin = FALSE)"))
        accountRepository.updateIsAdmin("new.admin@unipu.hr", true)
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM account WHERE email = 'new.admin@unipu.hr' AND is_admin = TRUE)"))
    }
    @Test
    fun `revoke admin`() {
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM account WHERE email = 'revoke.admin@unipu.hr' AND is_admin = TRUE)"))
        accountRepository.updateIsAdmin("revoke.admin@unipu.hr", false)
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM account WHERE email = 'revoke.admin@unipu.hr' AND is_admin = FALSE)"))
    }
    @Test
    fun `update root account password`() {
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM account WHERE email = 'root@unipu.hr')"))
        assertEquals(1, accountRepository.updateRootPassword("newPassword"))
        assertEquals(
            jdbcTemplate.queryForObject<String>("SELECT password FROM account WHERE email = 'root@unipu.hr'"),
            "newPassword"
        )
    }
    @Test
    fun `account exists by email`() {
        Assertions.assertTrue(accountRepository.existsByEmail("root@unipu.hr"))
        Assertions.assertFalse(accountRepository.existsByEmail("404@unipu.hr"))
    }
    @Test
    fun `retrieve account by id`() {
        assertEquals(
            Account(
                1,
                "root",
                "Mr.R",
                "root@unipu.hr",
                $$"$2a$12$rlJYGCjNYJpyZTt/enAIVuaI2JOdCyN93jQbE/hQQjIDLPTXmIOoC",
                "root Affiliation",
                "root job type",
                "root country",
                "root city",
                "root address",
                "root zip code",
                false
            ),
            accountRepository.byId(1)
        )
    }
    @Test
    fun `retrieve account by email`() {
        assertEquals(
            Account(
                1,
                "root",
                "Mr.R",
                "root@unipu.hr",
                $$"$2a$12$rlJYGCjNYJpyZTt/enAIVuaI2JOdCyN93jQbE/hQQjIDLPTXmIOoC",
                "root Affiliation",
                "root job type",
                "root country",
                "root city",
                "root address",
                "root zip code",
                false
            ),
            accountRepository.byEmail("root@unipu.hr")
        )
    }
    @Test
    fun `account exists by id`() {
        Assertions.assertTrue(accountRepository.existsById(1))
        Assertions.assertFalse(accountRepository.existsById(100))
    }
    @Test
    fun `retrieve all admin emails`() {
        assertEquals(
            listOf("admin1@unipu.hr", "admin2@unipu.hr", "revoke.admin@unipu.hr"),
            accountRepository.allAdminEmails()
        )
    }
    @Test
    fun `update account`() {
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
            AccountDTO(
                fullName = newAccount.fullName,
                title = newAccount.title,
                email = newAccount.email,
                password = newAccount.password,
                passwordConfirmation = newAccount.password,
                affiliation = newAccount.affiliation,
                jobType = newAccount.jobType,
                country = newAccount.country,
                city = newAccount.city,
                address = newAccount.address,
                zipCode = newAccount.zipCode,
            )
        )
        assertEquals(newAccount, accountRepository.byEmail(newAccount.email))
    }
    @Test
    fun `delete account`() {
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM account WHERE id = 1)"))
        assertEquals(1, accountRepository.delete(1))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM account WHERE id = 1)"))
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM account WHERE id = 2)"))
    }
}