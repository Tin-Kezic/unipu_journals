package hr.unipu.journals.feature.section.core

import hr.unipu.journals.feature.manuscript.core.ManuscriptState
import hr.unipu.journals.feature.manuscript.core.ManuscriptStateFilter
import hr.unipu.journals.feature.publication.core.Affiliation
import hr.unipu.journals.feature.publication.core.Sorting
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface SectionRepository: Repository<Section, Int> {
    @Query("SELECT * from publication_section WHERE id = :id")
    fun byId(@Param("id") sectionId: Int): Section

    @Query("""
        SELECT DISTINCT publication_section.title FROM publication_section
        JOIN publication ON publication_section.publication_id = publication.id
        LEFT JOIN manuscript ON manuscript.section_id = publication_section.id
        WHERE publication.title = :title
        AND publication.is_hidden = FALSE
        AND publication_section.is_hidden = FALSE
        ORDER BY publication_section.title
    """)
    fun allPublishedTitlesByPublicationTitle(@Param("title") publicationTitle: String): List<String>

    @Query("""
        SELECT publication_section.* FROM publication_section
        JOIN publication ON publication.id = publication_section.publication_id
        LEFT JOIN manuscript on manuscript.section_id = publication_section.id
        LEFT JOIN category ON manuscript.category_id = category.id
        LEFT JOIN eic_on_publication ON publication.id = eic_on_publication.publication_id AND :affiliation IS NOT NULL
        LEFT JOIN section_editor_on_section ON publication_section.id = section_editor_on_section.publication_section_id AND :affiliation IS NOT NULL
        LEFT JOIN account_role_on_manuscript ON manuscript.id = account_role_on_manuscript.manuscript_id
        LEFT JOIN account ON :account_id = account.id
        WHERE (category.name = :category OR :category IS NULL)
        AND (publication.id = :publication_id OR :publication_id IS NULL)
        AND (
            :manuscript_state_filter = 'HIDDEN' AND (
                publication.is_hidden = TRUE
                OR publication_section.is_hidden = TRUE
                OR manuscript.current_state = 'HIDDEN'
            )
            OR publication.is_hidden = FALSE AND publication_section.is_hidden = FALSE AND (
                :manuscript_state_filter = 'PUBLISHED' AND (
                    EXISTS (
                        SELECT 1 FROM manuscript m
                        WHERE m.section_id = publication_section.id
                        AND m.current_state = :manuscript_state_filter::manuscript_state
                    )
                    OR account.is_admin
                    OR eic_on_publication.eic_id = :account_id
                    OR section_editor_on_section.section_editor_id = :account_id
                ) AND (
                    :category IS NULL
                    OR
                    EXISTS (
                        SELECT 1 FROM manuscript m
                        JOIN category c on c.name = :category
                        WHERE m.category_id = category.id
                        AND m.current_state = :manuscript_state_filter::manuscript_state
                    )
                )
                OR
                :manuscript_state_filter = 'ARCHIVED' AND manuscript.current_state = 'ARCHIVED'
                OR
                account_role_on_manuscript.account_id = :account_id AND (
                    :manuscript_state_filter IN ('MINOR_MAJOR', 'REJECTED') AND manuscript.current_state IN ('MINOR', 'MAJOR', 'REJECTED')
                    OR
                    :manuscript_state_filter = 'ALL_AWAITING_REVIEW' AND (
                        account_role_on_manuscript.account_role = 'EIC' AND manuscript.current_state IN ('AWAITING_EIC_REVIEW', 'AWAITING_EDITOR_REVIEW', 'AWAITING_REVIEWER_REVIEW')
                        OR
                        account_role_on_manuscript.account_role = 'EDITOR' AND manuscript.current_state IN ('AWAITING_EDITOR_REVIEW', 'AWAITING_REVIEWER_REVIEW')
                        OR
                        account_role_on_manuscript.account_role = 'REVIEWER' AND manuscript.current_state = 'AWAITING_REVIEWER_REVIEW'
                    )
                    OR :manuscript_state_filter = 'AWAITING_EIC_REVIEW' AND (
                        manuscript.current_state = 'AWAITING_EIC_REVIEW' AND account_role_on_manuscript.account_role = 'EIC'
                    )
                    OR :manuscript_state_filter = 'AWAITING_EDITOR_REVIEW' AND (
                        manuscript.current_state = 'AWAITING_EDITOR_REVIEW' AND account_role_on_manuscript.account_role IN ('EIC', 'EDITOR')
                    )
                    OR :manuscript_state_filter = 'AWAITING_REVIEWER_REVIEW' AND (
                        manuscript.current_state = 'AWAITING_REVIEWER_REVIEW' AND account_role_on_manuscript.account_role IN ('EIC', 'EDITOR', 'REVIEWER')
                    )
                )
            )
        ) AND (
            :affiliation IS NULL
            OR (
                :affiliation = 'EIC_ON_PUBLICATION' AND eic_on_publication.eic_id = :account_id
                OR
                :affiliation = 'SECTION_EDITOR' AND section_editor_on_section.section_editor_id = :account_id
                OR
                account_role_on_manuscript.account_id = :account_id AND (
                    :affiliation = 'EIC_ON_MANUSCRIPT' AND account_role_on_manuscript.account_role = 'EIC'
                    OR
                    :affiliation = 'EDITOR' AND account_role_on_manuscript.account_role = 'EDITOR'
                    OR
                    :affiliation = 'REVIEWER' AND account_role_on_manuscript.account_role = 'REVIEWER'
                    OR
                    :affiliation = 'CORRESPONDING_AUTHOR' AND account_role_on_manuscript.account_role = 'CORRESPONDING_AUTHOR'
                    OR
                    :affiliation = 'AUTHOR' AND account_role_on_manuscript.account_role = 'AUTHOR'
                )
            )
        )
        AND (
            :sorting NOT IN ('NEWEST', 'OLDEST')
            OR EXISTS (
                SELECT 1
                FROM manuscript m
                JOIN publication_section ps ON ps.id = m.section_id
                WHERE ps.publication_id = publication.id
                AND (m.publication_date IS NOT NULL OR m.submission_date IS NOT NULL)
                AND m.current_state = :manuscript_state_filter::manuscript_state
            ) 
        )
        GROUP BY publication_section.id
        ORDER BY
            CASE WHEN :sorting = 'ALPHABETICAL_A_Z' THEN publication_section.title END,
            CASE WHEN :sorting = 'ALPHABETICAL_Z_A' THEN publication_section.title END DESC,
            CASE WHEN :sorting = 'NEWEST' THEN COALESCE(MAX(manuscript.publication_date), MAX(manuscript.submission_date)) END DESC,
            CASE WHEN :sorting = 'OLDEST' THEN COALESCE(MAX(manuscript.publication_date), MAX(manuscript.submission_date)) END,
            CASE WHEN :sorting = 'VIEWS' THEN MAX(manuscript.views) END
    """)
    fun all(
        @Param("publication_id") publicationId: Int,
        @Param("manuscript_state_filter") manuscriptStateFilter: ManuscriptStateFilter,
        @Param("affiliation") affiliation: Affiliation? = null,
        @Param("account_id") accountId: Int? = null,
        @Param("category") category: String? = null,
        @Param("sorting") sorting: Sorting? = null
    ): List<Section>

    @Modifying
    @Query("""
        INSERT INTO publication_section (title, publication_id)
        VALUES (:title, :publication_id)
    """)
    fun insert(
        @Param("title") title: String,
        @Param("publication_id") publicationId: Int,
    ): Int

    @Modifying
    @Query("""
        UPDATE publication_section SET
        title = COALESCE(:title, title),
        description = COALESCE(:description, description),
        publication_id = COALESCE(:publication_id, publication_id),
        is_hidden = COALESCE(:is_hidden, is_hidden)
        WHERE id = :id
    """)
    fun update(
        @Param("id") id: Int,
        @Param("title") title: String? = null,
        @Param("description") description: String? = null,
        @Param("publication_id") publicationId: Int? = null,
        @Param("is_hidden") isHidden: Boolean? = null,
    ): Int

    @Query("SELECT EXISTS (SELECT 1 FROM publication_section WHERE id = :id)")
    fun exists(@Param("id") id: Int): Boolean

    @Modifying
    @Query("DELETE FROM publication_section WHERE id = :id")
    fun delete(@Param("id") id: Int): Int
}