package hr.unipu.journals.data_jdbc_tests

import hr.unipu.journals.feature.category.CategoryRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DataJdbcTest
class CategoryRepositoryTests {
    @Autowired private lateinit var categoryRepository: CategoryRepository
    @Autowired private lateinit var jdbcTemplate: JdbcTemplate

    @Test fun `retrieve all categories`() {
        assertEquals(listOf("Biology", "Chemistry", "Computer Science", "Mathematics", "Physics"), categoryRepository.all())
    }
    @Test fun `insert category`() {
        categoryRepository.insert("new category")
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM category WHERE name = 'new category')"))
    }
    @Test fun `delete category`() {
        categoryRepository.delete("Physics")
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM category WHERE name = 'Physics')"))
    }
}