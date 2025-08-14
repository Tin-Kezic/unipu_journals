package hr.unipu.journals.feature.section_editor_on_section

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

private const val SECTION_EDITOR_ON_SECTION = "section_editor_on_section"
private const val ID = "id"
private const val PUBLICATION_SECTION_ID = "publication_section_id"
private const val SECTION_EDITOR_ID = "section_editor_id"
interface SectionEditorOnSectionRepository: Repository<SectionEditorOnSection, Int> {
    @Query("SELECT EXISTS (SELECT 1 FROM $SECTION_EDITOR_ON_SECTION WHERE $SECTION_EDITOR_ID = :$SECTION_EDITOR_ID AND $PUBLICATION_SECTION_ID = :$PUBLICATION_SECTION_ID")
    fun isSectionEditorOnSection(@Param(SECTION_EDITOR_ID) sectionEditorId: Int, @Param(PUBLICATION_SECTION_ID) sectionId: Int)
}