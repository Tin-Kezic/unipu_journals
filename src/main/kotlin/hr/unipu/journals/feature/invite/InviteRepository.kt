package hr.unipu.journals.feature.invite

import hr.unipu.journals.feature.manuscript.core.InvitedManuscript
import hr.unipu.journals.feature.manuscript.core.ManuscriptStateFilter
import hr.unipu.journals.feature.publication.core.Role
import hr.unipu.journals.feature.publication.core.Sorting
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface InviteRepository: Repository<Invite, Int> {

    @Query("SELECT EXISTS (SELECT 1 FROM invite WHERE email = :email AND target = 'ADMIN')")
    fun isAdmin(@Param("email") email: String): Boolean

    @Query("SELECT * FROM invite WHERE email = :email")
    fun allByEmail(@Param("email") email: String): List<Invite>

    @Query("SELECT invite.email FROM invite WHERE invite.target = :target::invitation_target AND (invite.target_id = :target_id OR :target_id IS NULL)")
    fun emailsByTarget(@Param("target") target: InvitationTarget, @Param("target_id") targetId: Int? = null): List<String>

    @Query("""
        SELECT
            invite.target as role,
            manuscript.*
        FROM invite
        JOIN manuscript ON invite.target_id = manuscript.id
        JOIN publication_section ON manuscript.section_id = publication_section.id
        JOIN publication on publication_section.publication_id = publication.id
        JOIN category ON manuscript.category_id = category.id
        LEFT JOIN eic_on_publication ON publication.id = eic_on_publication.publication_id
        LEFT JOIN section_editor_on_section ON publication_section.id = section_editor_on_section.publication_section_id AND :role IS NOT NULL
        LEFT JOIN account_role_on_manuscript ON manuscript.id = account_role_on_manuscript.manuscript_id
        WHERE (category.name = :category OR :category IS NULL)
        AND publication_section.is_hidden = FALSE AND publication.is_hidden = FALSE
        AND (publication_section.id = :section_id OR :section_id IS NULL)
        AND invite.email = :email
        AND (
            :manuscript_state_filter = 'ALL_AWAITING_REVIEW' AND (
                invite.target = 'EIC_ON_MANUSCRIPT' AND manuscript.current_state IN ('AWAITING_EIC_REVIEW', 'AWAITING_EDITOR_REVIEW', 'AWAITING_REVIEWER_REVIEW')
                OR
                invite.target = 'EDITOR' AND manuscript.current_state IN ('AWAITING_EDITOR_REVIEW', 'AWAITING_REVIEWER_REVIEW')
                OR
                invite.target = 'REVIEWER' AND manuscript.current_state = 'AWAITING_REVIEWER_REVIEW'
            )
            OR :manuscript_state_filter = 'AWAITING_EIC_REVIEW'
                AND manuscript.current_state = 'AWAITING_EIC_REVIEW'
                AND invite.target = 'EIC_ON_MANUSCRIPT'
            OR :manuscript_state_filter = 'AWAITING_EDITOR_REVIEW'
                AND manuscript.current_state = 'AWAITING_EDITOR_REVIEW'
                AND invite.target IN ('EIC_ON_MANUSCRIPT', 'EDITOR')
            OR :manuscript_state_filter = 'AWAITING_REVIEWER_REVIEW'
                AND manuscript.current_state = 'AWAITING_REVIEWER_REVIEW'
                AND invite.target IN ('EIC_ON_MANUSCRIPT', 'EDITOR', 'REVIEWER')
        ) AND (
            :role IS NULL
            OR (
                :role = 'EIC_ON_PUBLICATION' AND eic_on_publication.eic_id = :account_id
                OR
                :role = 'SECTION_EDITOR' AND section_editor_on_section.section_editor_id = :account_id
                OR
                account_role_on_manuscript.account_id = :account_id AND (
                    :role = 'EIC_ON_MANUSCRIPT' AND account_role_on_manuscript.account_role = 'EIC'
                    OR
                    :role = 'EDITOR' AND account_role_on_manuscript.account_role = 'EDITOR'
                    OR
                    :role = 'REVIEWER' AND account_role_on_manuscript.account_role = 'REVIEWER'
                    OR
                    :role = 'AUTHOR' AND account_role_on_manuscript.account_role = 'AUTHOR'
                )
            )
        )
        GROUP BY invite.id, manuscript.id
        ORDER BY
            CASE WHEN :sorting = 'ALPHABETICAL_A_Z' THEN manuscript.title END,
            CASE WHEN :sorting = 'ALPHABETICAL_Z_A' THEN manuscript.title END DESC,
            CASE WHEN :sorting = 'NEWEST' THEN COALESCE(manuscript.publication_date, manuscript.submission_date) END DESC,
            CASE WHEN :sorting = 'OLDEST' THEN COALESCE(manuscript.publication_date, manuscript.submission_date) END
    """)
    fun invitedManuscripts(
        @Param("email") email: String,
        @Param("manuscript_state_filter") manuscriptStateFilter: ManuscriptStateFilter,
        @Param("role") role: Role?,
        @Param("account_id") accountId: Int,
        @Param("section_id") sectionId: Int?,
        @Param("category") category: String?,
        @Param("sorting") sorting: Sorting
    ): List<InvitedManuscript>

    @Modifying
    @Query("INSERT INTO invite (email, target, target_id) VALUES (:email, :target::invitation_target, :target_id)")
    fun invite(@Param("email") email: String, @Param("target") target: InvitationTarget, @Param("target_id") targetId: Int = 0): Int

    @Modifying
    @Query("DELETE FROM invite WHERE email = :email AND (target = :target::invitation_target OR :target IS NULL) AND (target_id = :target_id OR :target_id IS NULL)")
    fun revoke(@Param("email") email: String, @Param("target") target: InvitationTarget? = null, @Param("target_id") targetId: Int? = null): Int
}