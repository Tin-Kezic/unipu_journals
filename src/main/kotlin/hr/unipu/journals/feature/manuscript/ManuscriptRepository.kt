package hr.unipu.journals.feature.manuscript

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

interface ManuscriptRepository: Repository<Manuscript, Int> {

    @Query("""
        SELECT * FROM manuscript
        WHERE (id = :id OR :id IS NULL)
        AND (title = :title OR :title IS NULL)
        AND (description = :description OR :description IS NULL)
        AND (author_id = :author_id OR :author_id IS NULL)
        AND (category_id = :category_id OR :category_id IS NULL)
        AND (current_state = :state OR :state IS NULL)
        AND (section_id = :section_id OR :section_id IS NULL)
        AND (file_url = :file_url OR :file_url IS NULL)
        AND (submission_date = :submission_date OR :submission_date IS NULL)
        AND (publication_date = :publication_date OR :publication_date IS NULL)
        AND (views = :views OR :views IS NULL)
        AND (downloads = :downloads OR :downloads IS NULL)
        ORDER BY id DESC
        """)
    fun all(
        @Param("id") id: Int? = null,
        @Param("title") title: String? = null,
        @Param("description") description: String? = null,
        @Param("author_id") authorId: Int? = null,
        @Param("category_id") categoryId: Int? = null,
        @Param("state") manuscriptState: ManuscriptState? = null,
        @Param("section_id") sectionId: Int? = null,
        @Param("file_url") fileUrl: String? = null,
        @Param("submission_date") submissionDate: LocalDateTime? = null,
        @Param("publication_date") publicationDate: LocalDateTime? = null,
        @Param("views") views: Int? = null,
        @Param("downloads") downloads: Int? = null
    ): List<Manuscript>

    @Modifying
    @Transactional
    @Query("UPDATE manuscript SET views = views + 1 WHERE id = :id")
    fun incrementViews(@Param("id") id: Int)

    @Modifying
    @Query("UPDATE manuscript SET downloads = downloads + 1 WHERE id = :id")
    fun incrementDownloads(@Param("id") id: Int)

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
    )
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
    fun updateState(@Param("id") id: Int, @Param("state") manuscriptState: ManuscriptState)

    @Modifying
    @Query("DELETE FROM manuscript WHERE id = :id")
    fun delete(@Param("id") id: Int)

    @Query("SELECT * FROM manuscript WHERE id = :id")
    fun byId(@Param("id") manuscriptId: Int): Manuscript

    @Query("""
        SELECT manuscript.* FROM manuscript
        JOIN account ON manuscript.author_id = account.id
        WHERE manuscript.author_id = account.id
    """)
    fun allByAuthor(@Param("id") authorId: Int): List<Manuscript>
}