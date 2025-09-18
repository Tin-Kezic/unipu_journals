package hr.unipu.journals.feature.section_editor_on_section

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

private const val ID = "id"
private const val SECTION_EDITOR_ON_SECTION = "section_editor_on_section"
private const val PUBLICATION_SECTION_ID = "publication_section_id"
private const val SECTION_EDITOR_ID = "section_editor_id"

private const val ACCOUNT = "account"
//private const val ID = "id"
private const val FULL_NAME = "full_name"
private const val TITLE = "title"
private const val EMAIL = "email"
private const val PASSWORD = "password"
private const val AFFILIATION = "affiliation"
private const val JOB_TYPE = "job_type"
private const val COUNTRY = "country"
private const val CITY = "city"
private const val ADDRESS = "address"
private const val ZIP_CODE = "zip_code"
private const val IS_ADMIN = "is_admin"

interface SectionEditorOnSectionRepository: Repository<SectionEditorOnSection, Int> {
    @Query("SELECT EXISTS (SELECT 1 FROM $SECTION_EDITOR_ON_SECTION WHERE $SECTION_EDITOR_ID = :$SECTION_EDITOR_ID AND $PUBLICATION_SECTION_ID = :$PUBLICATION_SECTION_ID)")
    fun isSectionEditorOnSection(@Param(SECTION_EDITOR_ID) sectionEditorId: Int, @Param(PUBLICATION_SECTION_ID) sectionId: Int): Boolean

    @Query("""
        SELECT $ACCOUNT.$EMAIL FROM $SECTION_EDITOR_ON_SECTION
        JOIN $ACCOUNT ON $SECTION_EDITOR_ON_SECTION.$SECTION_EDITOR_ID = $ACCOUNT.$ID
        WHERE $SECTION_EDITOR_ON_SECTION.$PUBLICATION_SECTION_ID = :$PUBLICATION_SECTION_ID
    """)
    fun sectionEditorEmailsBySectionId(@Param(PUBLICATION_SECTION_ID) sectionId: Int): List<String>

    @Modifying
    @Query("INSERT INTO $SECTION_EDITOR_ON_SECTION ($PUBLICATION_SECTION_ID, $SECTION_EDITOR_ID) VALUES (:$PUBLICATION_SECTION_ID, :$SECTION_EDITOR_ID)")
    fun assign(@Param(PUBLICATION_SECTION_ID) sectionId: Int, @Param(SECTION_EDITOR_ID) sectionEditorId: Int)

    @Modifying
    @Query("DELETE FROM $SECTION_EDITOR_ON_SECTION WHERE $PUBLICATION_SECTION_ID = :$PUBLICATION_SECTION_ID AND $SECTION_EDITOR_ID = :$SECTION_EDITOR_ID")
    fun revoke(@Param(PUBLICATION_SECTION_ID) sectionId: Int, @Param(SECTION_EDITOR_ID) sectionEditorId: Int)
}