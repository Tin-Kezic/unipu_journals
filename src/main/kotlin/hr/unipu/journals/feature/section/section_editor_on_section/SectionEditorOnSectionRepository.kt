package hr.unipu.journals.feature.section.section_editor_on_section

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface SectionEditorOnSectionRepository: Repository<SectionEditorOnSection, Int> {
    @Query("SELECT EXISTS (SELECT 1 FROM section_editor_on_section WHERE section_editor_id = :section_editor_id AND publication_section_id = :publication_section_id)")
    fun isSectionEditorOnSection(@Param("section_editor_id") sectionEditorId: Int, @Param("publication_section_id") sectionId: Int): Boolean

    @Query("""
        SELECT account.email FROM section_editor_on_section
        JOIN account ON section_editor_on_section.section_editor_id = account.id
        WHERE section_editor_on_section.publication_section_id = :publication_section_id
    """)
    fun sectionEditorEmailsBySectionId(@Param("publication_section_id") sectionId: Int): List<String>

    @Query("SELECT publication_section_id FROM section_editor_on_section WHERE section_editor_id = :section_editor_id")
    fun allAffiliatedSectionIds(@Param("section_editor_id") sectionEditorId: Int): List<Int>

    @Modifying
    @Query("INSERT INTO section_editor_on_section (section_editor_id, publication_section_id) VALUES (:section_editor_id, :publication_section_id)")
    fun assign(@Param("section_editor_id") sectionEditorId: Int, @Param("publication_section_id") sectionId: Int): Int

    @Modifying
    @Query("DELETE FROM section_editor_on_section WHERE section_editor_id = :section_editor_id AND (publication_section_id = :publication_section_id OR :publication_section_id IS NULL)")
    fun revoke(@Param("section_editor_id") sectionEditorId: Int, @Param("publication_section_id") sectionId: Int? = null): Int
}