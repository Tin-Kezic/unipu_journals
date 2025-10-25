package hr.unipu.journals.repository_tests

import hr.unipu.journals.feature.section.core.SectionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.test.Test
import kotlin.test.assertEquals

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
            listOf(),
            sectionRepository.allByPublicationId(1)
        )
    }
}