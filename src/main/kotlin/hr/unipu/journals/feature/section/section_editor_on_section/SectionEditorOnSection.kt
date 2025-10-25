package hr.unipu.journals.feature.section.section_editor_on_section

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("section_editor_on_section")
data class SectionEditorOnSection(
    @Id val id: Int,
    val publicationSectionId: Int,
    val sectionEditorId: Int,
)