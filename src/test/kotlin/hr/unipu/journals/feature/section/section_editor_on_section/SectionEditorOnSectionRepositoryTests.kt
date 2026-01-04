package hr.unipu.journals.feature.section.section_editor_on_section

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject

@DataJdbcTest
class SectionEditorOnSectionRepositoryTests {
    @Autowired private lateinit var sectionEditorOnSectionRepository: SectionEditorOnSectionRepository
    @Autowired private lateinit var jdbcTemplate: JdbcTemplate

    @Test fun `check is section editor on section by section editor id and section id`() {
        assertTrue(sectionEditorOnSectionRepository.isSectionEditorOnSection(9, 1))
        assertFalse(sectionEditorOnSectionRepository.isSectionEditorOnSection(9, 2))
        assertTrue(sectionEditorOnSectionRepository.isSectionEditorOnSection(10, 2))
        assertFalse(sectionEditorOnSectionRepository.isSectionEditorOnSection(10, 1))
    }
    @Test fun `retrieve section editor emails by section id`() {
        assertEquals(listOf("section.editor1@unipu.hr"), sectionEditorOnSectionRepository.sectionEditorEmailsBySectionId(1))
        assertEquals(listOf("section.editor2@unipu.hr"), sectionEditorOnSectionRepository.sectionEditorEmailsBySectionId(2))
    }
    @Test fun `assign section editor on section`() {
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM section_editor_on_section WHERE section_editor_id = 28 AND publication_section_id = 3)"))
        sectionEditorOnSectionRepository.assign(3, 28)
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM section_editor_on_section WHERE section_editor_id = 28 AND publication_section_id = 3)"))
    }
    @Test fun `revoke section editor on section`() {
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM section_editor_on_section WHERE section_editor_id = 29 AND publication_section_id = 4)"))
        sectionEditorOnSectionRepository.revoke(29, 4)
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM section_editor_on_section WHERE section_editor_id = 29 AND publication_section_id = 4)"))
    }
}