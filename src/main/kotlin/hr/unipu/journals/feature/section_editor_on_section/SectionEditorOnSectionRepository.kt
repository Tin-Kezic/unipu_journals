package hr.unipu.journals.feature.section_editor_on_section

import org.springframework.data.repository.Repository

private const val SECTION_EDITOR_ON_SECTION = "section_editor_on_section"
private const val ID = "id"
private const val PUBLICATION_SECTION_ID = "publication_section_id"
private const val SECTION_EDITOR_ID = "section_editor_id"
interface SectionEditorOnSectionRepository: Repository<SectionEditorOnSection, Int>