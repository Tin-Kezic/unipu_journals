package hr.unipu.journals.feature.manuscript

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface ManuscriptRepository: Repository<Manuscript, Int> {
    @Query("SELECT * FROM manuscript WHERE section_id = :section_id AND (current_state = :state OR :state IS NULL) ORDER BY id DESC")
    fun allBySectionId(@Param("section_id") sectionId: Int, @Param("state") manuscriptState: ManuscriptState? = null): List<Manuscript>

    @Query("SELECT * FROM manuscript WHERE author_id = :author_id AND (current_state = :state OR :state IS NULL) ORDER BY id DESC")
    fun allByAuthorId(@Param("author_id") authorId: Int, @Param("state") manuscriptState: ManuscriptState? = null): List<Manuscript>

    @Modifying
    @Transactional
    @Query("UPDATE manuscript SET views = views + 1 WHERE id = :id")
    fun incrementViews(@Param("id") id: Int): Int

    @Modifying
    @Query("UPDATE manuscript SET downloads = downloads + 1 WHERE id = :id")
    fun incrementDownloads(@Param("id") id: Int): Int

    @Query("""
        SELECT publication.id AS publication_id, publication_section.id AS section_id FROM manuscript
        JOIN publication_section ON manuscript.section_id = publication_section.id
        JOIN publication ON publication_section.publication_id = publication.id
        WHERE manuscript.id = :id
        """)
    fun publicationIdAndSectionId(@Param("id") manuscriptId: Int): PublicationIdAndSectionIdDTO

    @Modifying
    @Query("INSERT INTO manuscript (title, author_id, category_id, section_id, file_url) VALUES (:title, :author_id, :category_id, :section_id, :file_url)")
    fun insert(
        @Param("title") title: String,
        @Param("author_id") authorId: Int,
        @Param("category_id") categoryId: Int,
        @Param("section_id") sectionId: Int,
        @Param("file_url") fileUrl: String
    ): Int
    @Query("""
        SELECT DISTINCT manuscript.* FROM manuscript
        JOIN publication_section ON manuscript.section_id = publication_section.id
        JOIN publication ON publication_section.publication_id = publication.id
        JOIN account_role_on_manuscript ON manuscript.id = account_role_on_manuscript.manuscript_id
        WHERE account_role_on_manuscript.account_id = :account_id
        AND publication.is_hidden = FALSE
        AND publication_section.is_hidden = FALSE
        AND (publication.id = :publication_id OR :publication_id IS NULL)
        AND ((
            account_role_on_manuscript.account_role = 'EIC'
            AND manuscript.current_state IN ('AWAITING_EIC_REVIEW', 'AWAITING_EDITOR_REVIEW', 'AWAITING_REVIEWER_REVIEW')
        ) OR (
            account_role_on_manuscript.account_role = 'EDITOR'
            AND manuscript.current_state IN ('AWAITING_EDITOR_REVIEW', 'AWAITING_REVIEWER_REVIEW')
        ) OR (
            account_role_on_manuscript.account_role = 'REVIEWER'
            AND manuscript.current_state = 'AWAITING_REVIEWER_REVIEW'
        ))
    """)
    fun pending(@Param("account_id") accountId: Int, @Param("publication_id") publicationId: Int? = null): List<Manuscript>

    @Query("SELECT EXISTS (SELECT 1 FROM manuscript WHERE id = :id)")
    fun exists(@Param("id") id: Int): Boolean

    @Modifying
    @Query("UPDATE manuscript SET current_state = :state WHERE id = :id")
    fun updateState(@Param("id") id: Int, @Param("state") manuscriptState: ManuscriptState): Int

    @Modifying
    @Query("DELETE FROM manuscript WHERE id = :id")
    fun delete(@Param("id") id: Int)

    @Query("SELECT * FROM manuscript WHERE id = :id")
    fun byId(@Param("id") manuscriptId: Int): Manuscript?

    @Query("""
        SELECT manuscript.* FROM manuscript
        JOIN account ON manuscript.author_id = account.id
        WHERE manuscript.author_id = account.id
    """)
    fun allByAuthor(@Param("id") authorId: Int): List<Manuscript>
}