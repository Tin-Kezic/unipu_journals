package hr.unipu.journals.feature.manuscript.category

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject

@DataJdbcTest
class CategoryRepositoryTests {
    @Autowired private lateinit var categoryRepository: CategoryRepository
    @Autowired private lateinit var jdbcTemplate: JdbcTemplate

    @Test fun `retrieve all categories`() {
        assertEquals(
            listOf("Biology", "Chemistry", "Computer Science", "Mathematics", "Physics"),
            categoryRepository.all()
        )
    }
    @Test fun `insert category`() {
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM category WHERE name = 'new category')"))
        assertEquals(1, categoryRepository.insert("new category"))
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM category WHERE name = 'new category')"))
    }
    @Test fun `delete category`() {
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM category WHERE name = 'Physics')"))
        assertEquals(1, categoryRepository.delete("Physics"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM category WHERE name = 'Physics')"))
    }
}