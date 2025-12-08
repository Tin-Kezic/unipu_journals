package hr.unipu.journals.feature.invite

import hr.unipu.journals.feature.manuscript.core.Manuscript
import hr.unipu.journals.feature.manuscript.core.ManuscriptStateFilter
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
        SELECT DISTINCT manuscript.* FROM invite
        JOIN manuscript ON invite.target_id = manuscript.id
        JOIN publication_section ON manuscript.section_id = publication_section.id
        WHERE publication_section.is_hidden = FALSE
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
            OR :manuscript_state_filter = 'AWAITING_EIC_REVIEW' AND (
                manuscript.current_state = 'AWAITING_EIC_REVIEW' AND invite.target = 'EIC_ON_MANUSCRIPT'
            )
            OR :manuscript_state_filter = 'AWAITING_EDITOR_REVIEW' AND (
                manuscript.current_state = 'AWAITING_EDITOR_REVIEW' AND invite.target = 'EDITOR'
            )
            OR :manuscript_state_filter = 'AWAITING_REVIEWER_REVIEW' AND (
                manuscript.current_state = 'AWAITING_REVIEWER_REVIEW' AND invite.target = 'REVIEWER'
            )
        )
    """)
    fun affiliatedManuscripts(
        @Param("email") email: String,
        @Param("manuscript_state_filter") manuscriptStateFilter: ManuscriptStateFilter,
        @Param("section_id") sectionId: Int,
    ): List<Manuscript>

    @Modifying
    @Query("INSERT INTO invite (email, target, target_id) VALUES (:email, :target::invitation_target, :target_id)")
    fun invite(@Param("email") email: String, @Param("target") target: InvitationTarget, @Param("target_id") targetId: Int = 0): Int

    @Modifying
    @Query("DELETE FROM invite WHERE email = :email AND target = :target::invitation_target AND (target_id = :target_id OR :target_id IS NULL)")
    fun revoke(@Param("email") email: String, @Param("target") target: InvitationTarget, @Param("target_id") targetId: Int? = null): Int
}