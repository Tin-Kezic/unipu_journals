package hr.unipu.journals.feature.invite

import hr.unipu.journals.feature.manuscript.core.InvitedManuscript
import hr.unipu.journals.feature.manuscript.core.ManuscriptStateFilter
import hr.unipu.journals.feature.publication.core.Sorting
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface InviteRepository: Repository<Invite, Int> {

    @Query("SELECT EXISTS (SELECT 1 FROM invite WHERE email = :email AND target = 'ADMIN')")
    fun isAdmin(@Param("email") email: String): Boolean

    @Query("SELECT invite.email FROM invite WHERE invite.target = :target::invitation_target AND (invite.target_id = :target_id OR :target_id IS NULL)")
    fun emailsByTarget(@Param("target") target: InvitationTarget, @Param("target_id") targetId: Int? = null): List<String>

    @Query("""
        SELECT
            array_agg(DISTINCT invite.target) AS affiliations,
            manuscript.*
        FROM invite
        JOIN manuscript ON invite.target_id = manuscript.id
        JOIN publication_section ON manuscript.section_id = publication_section.id
        JOIN publication on publication_section.publication_id = publication.id
        WHERE publication_section.is_hidden = FALSE AND publication.is_hidden = FALSE
        AND publication_section.id = :section_id
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
        )
        GROUP BY invite.email, invite.target_id, manuscript.id
        ORDER BY
            CASE WHEN :sorting = 'ALPHABETICAL_A_Z' THEN manuscript.title END,
            CASE WHEN :sorting = 'ALPHABETICAL_Z_A' THEN manuscript.title END DESC,
            CASE WHEN :sorting = 'NEWEST' THEN COALESCE(manuscript.publication_date, manuscript.submission_date) END DESC,
            CASE WHEN :sorting = 'OLDEST' THEN COALESCE(manuscript.publication_date, manuscript.submission_date) END
    """)
    fun invitedManuscripts(
        @Param("email") email: String,
        @Param("manuscript_state_filter") manuscriptStateFilter: ManuscriptStateFilter,
        @Param("section_id") sectionId: Int,
        @Param("sorting") sorting: Sorting
    ): List<InvitedManuscript>

    @Modifying
    @Query("INSERT INTO invite (email, target, target_id) VALUES (:email, :target::invitation_target, :target_id)")
    fun invite(@Param("email") email: String, @Param("target") target: InvitationTarget, @Param("target_id") targetId: Int = 0): Int

    @Modifying
    @Query("DELETE FROM invite WHERE email = :email AND target = :target::invitation_target AND (target_id = :target_id OR :target_id IS NULL)")
    fun revoke(@Param("email") email: String, @Param("target") target: InvitationTarget, @Param("target_id") targetId: Int? = null): Int
}