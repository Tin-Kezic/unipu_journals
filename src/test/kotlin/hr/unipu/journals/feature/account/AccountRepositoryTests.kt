package hr.unipu.journals.feature.account

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
    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private val testAccount = Account(
        id = 1,
        fullName = "John Doe",
        title = "Dr.",
        email = "john.doe@example.com",
        password = "secure123",
        affiliation = "University",
        jobType = "Professor",
        country = "USA",
        city = "Cheyenne",
        address = "200 West 24th Street",
        zipCode = "82002-0020",
        isAdmin = false
    )
    fun insertAccount(
        fullName: String = testAccount.fullName,
        title: String = testAccount.title,
        email: String = testAccount.email,
        password: String = testAccount.password,
        affiliation: String = testAccount.affiliation,
        jobType: String = testAccount.jobType,
        country: String = testAccount.country,
        city: String = testAccount.city,
        address: String = testAccount.address,
        zipCode: String = testAccount.zipCode
    ) = accountRepository.insert(fullName, title, email, password, affiliation, jobType, country, city, address, zipCode)

    @Test
    fun `assign and revoke admin, check is admin`() {
        // Arrange
        insertAccount(email = "user@email.com")
        insertAccount(email = "admin@email.com")
        insertAccount(email = "revokedAdmin@email.com")

        // act
        accountRepository.updateIsAdmin("admin@email.com", true)
        accountRepository.updateIsAdmin("revokedAdmin@email.com", true)
        accountRepository.updateIsAdmin("revokedAdmin@email.com", false)

        // assert
        assertTrue(accountRepository.isAdmin("admin@email.com"))
        assertFalse(accountRepository.isAdmin("user@email.com"))
        assertFalse(accountRepository.isAdmin("revokedAdmin@email.com"))
    }
    @Test
    fun `update root account password`() {
        // arrange
        insertAccount(email = "root@unipu.hr")
        // act
        accountRepository.updateRootPassword("newPassword")
        // assert
        assertEquals("newPassword", accountRepository.byEmail("root@unipu.hr")?.password)
    }
    @Test
    fun `account exists by id`() {
        // arrange
        insertAccount()
        val id = jdbcTemplate.queryForObject<Int>("SELECT MAX(id) FROM account")
        // act
        val accountExists = accountRepository.exists(id)
        // assert
        assertTrue(accountExists)
    }
    @Test
    fun `account exists by email`() {
        // arrange
        insertAccount()
        // act
        val accountExists = accountRepository.emailExists(testAccount.email)
        // assert
        assertTrue(accountExists)
    }
    @Test
    fun `retrieve account by id`() {
        // arrange
        insertAccount()
        val id = jdbcTemplate.queryForObject<Int>("SELECT MAX(id) FROM account")
        // act
        val account = accountRepository.byId(id)
        // assert
        assertEquals(testAccount.copy(id = id), account)
    }
    @Test
    fun `retrieve account by email`() {
        // arrange
        insertAccount()
        // act
        val account = accountRepository.byEmail(testAccount.email)
        // assert
        assertEquals(testAccount, account)
    }
    @Test
    fun `retrieve all admin emails`() {
        // arrange
        insertAccount(email = "user1@email.com")
        insertAccount(email = "user2@email.com")
        insertAccount(email = "admin1@email.com")
        insertAccount(email = "admin2@email.com")
        insertAccount(email = "admin3@email.com")
        // act
        accountRepository.updateIsAdmin("admin1@email.com", true)
        accountRepository.updateIsAdmin("admin2@email.com", true)
        accountRepository.updateIsAdmin("admin3@email.com", true)
        // assert
        assertTrue(accountRepository.isAdmin("admin1@email.com"))
        assertTrue(accountRepository.isAdmin("admin2@email.com"))
        assertTrue(accountRepository.isAdmin("admin3@email.com"))
        assertFalse(accountRepository.isAdmin("user1@email.com"))
        assertFalse(accountRepository.isAdmin("user2@email.com"))
    }
    @Test
    fun `update account`() {
        // arrange
        insertAccount()
        val id = jdbcTemplate.queryForObject<Int>("SELECT MAX(id) FROM account")
        val newAccount = Account(
            id = id,
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
        // act
        accountRepository.update(
            id = id,
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
        // assert
        assertEquals( newAccount, accountRepository.byEmail(newAccount.email))
    }
    @Test
    fun `delete account`() {
        // arrange
        insertAccount()
        val id = jdbcTemplate.queryForObject<Int>("SELECT MAX(id) FROM account")
        insertAccount(email = "user@email.com")
        // act
        accountRepository.delete(id)
        // assert
        assertNull(accountRepository.byId(id))
        assertNotNull(accountRepository.byId(id + 1))
    }
}