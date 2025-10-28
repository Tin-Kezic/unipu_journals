package hr.unipu.journals.feature.section.core

import hr.unipu.journals.feature.manuscript.core.ManuscriptState
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject

@DataJdbcTest
class SectionRepositoryTests {
    @Autowired private lateinit var sectionRepository: SectionRepository
    @Autowired private lateinit var jdbcTemplate: JdbcTemplate

    @Test fun `retrieve section title by section id`() {
        assertEquals(
            "Machine Learning",
            sectionRepository.byId(1).title
        )
    }
    @Test fun `retrieve section titles by publication title`() {
        assertEquals(
            listOf("Computer Vision", "Deep Learning", "Natural Language Processing"),
            sectionRepository.allPublishedTitlesByPublicationTitle("Journal of AI Research")
        )
    }
    @Test fun `retrieve all sections by publication id`() {
        assertEquals(
            listOf(
                Section(4, "Computer Vision", "Image processing and visual recognition", 1, false),
                Section(3, "Natural Language Processing", "Language models and text analysis", 1, false),
                Section(2, "Deep Learning", "Neural networks and deep learning models", 1, false),
            ),
            sectionRepository.allByPublicationId(1)
        )
    }
    @Test fun `retrieve all sections which contain archived manuscripts by publication id and manuscript state`() {
        assertEquals(
            listOf(Section(2, "Deep Learning", "Neural networks and deep learning models", 1, false)),
            sectionRepository.allByPublicationId(1, ManuscriptState.ARCHIVED)
        )
    }

    @Test fun `insert section`() {
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM publication_section WHERE title = 'new section' AND publication_id = 3)"))
        sectionRepository.insert("new section", 3)
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM publication_section WHERE title = 'new section' AND publication_id = 3)"))
    }
    @Test fun `update section`() {
        assertTrue(
            jdbcTemplate.queryForObject<Boolean>(
                """
            SELECT EXISTS (SELECT 1 FROM publication_section
            WHERE id = 1
            AND title = 'Machine Learning'
            AND description = 'ML research and techniques'
            AND publication_id = 1
            AND is_hidden = TRUE
            )""".trimIndent()
            )
        )
        sectionRepository.update(
            id = 1,
            title = "new Machine Learning",
            description = "new ML research and techniques",
            publicationId = 2,
            isHidden = false
        )
        Assertions.assertFalse(
            jdbcTemplate.queryForObject<Boolean>(
                """
            SELECT EXISTS (SELECT 1 FROM publication_section
            WHERE id = 1
            AND title = 'Machine Learning'
            AND description = 'ML research and techniques'
            AND publication_id = 1
            AND is_hidden = TRUE
            )""".trimIndent()
            )
        )
        assertTrue(
            jdbcTemplate.queryForObject<Boolean>(
                """
            SELECT EXISTS (SELECT 1 FROM publication_section
            WHERE id = 1
            AND title = 'new Machine Learning'
            AND description = 'new ML research and techniques'
            AND publication_id = 2
            AND is_hidden = FALSE
            )""".trimIndent()
            )
        )
    }
    @Test fun `check if section exists by id`() {
        assertTrue(sectionRepository.exists(1))
        Assertions.assertFalse(sectionRepository.exists(1000))
    }
    @Test fun `delete section by id`() {
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM publication_section WHERE id = 1)"))
        sectionRepository.delete(1)
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM publication_section WHERE id = 1)"))
    }
}