package hr.unipu.journals.feature.invite

import hr.unipu.journals.feature.manuscript.Manuscript
import hr.unipu.journals.feature.publication.Publication
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface InviteRepository: Repository<Invite, Int> {

    @Query("SELECT EXISTS (SELECT 1 FROM invite WHERE email = :email AND target = 'ADMIN')")
    fun isAdmin(@Param("email") email: String): Boolean

    @Query("SELECT invite.email FROM invite WHERE invite.target = :target AND (invite.target_id = :target_id OR :target_id IS NULL)")
    fun emailsByTarget(@Param("target") target: InvitationTarget, @Param("target_id") targetId: Int? = null): List<String>

    @Query("""
        SELECT DISTINCT publication.* FROM invite
        JOIN manuscript ON invite.target_id = manuscript.id
        JOIN publication_section ON manuscript.section_id = publication_section.id
        JOIN publication ON publication_section.publication_id = publication.id
        WHERE invite.email = :email
        AND manuscript.current_state IN ('AWAITING_INITIAL_EIC_REVIEW', 'AWAITING_INITIAL_EDITOR_REVIEW', 'AWAITING_REVIEWER_REVIEW', 'MINOR', 'MAJOR')
        AND publication.is_hidden = FALSE
        AND publication_section.is_hidden = FALSE
    """)
    fun allPublicationsWhichContainManuscriptsUnderReviewWithAffiliation(@Param("email") email: String): List<Publication>

    @Query("""
        SELECT DISTINCT manuscript.* FROM invite
        JOIN manuscript ON invite.target_id = manuscript.id
        JOIN publication_section ON manuscript.section_id = publication_section.id
        JOIN publication ON publication_section.publication_id = publication.id
        WHERE invite.target IN ('EIC_ON_MANUSCRIPT', 'EDITOR_ON_MANUSCRIPT', 'REVIEWER_ON_MANUSCRIPT')
        AND manuscript.current_state IN ('AWAITING_INITIAL_EIC_REVIEW', 'AWAITING_INITIAL_EDITOR_REVIEW', 'AWAITING_REVIEWER_REVIEW')
        AND publication.is_hidden = FALSE
        AND publication_section.is_hidden = FALSE
        AND invite.email = :email
        AND (publication.id = :publication_id OR :publication_id IS NULL)
    """)
    fun affiliatedManuscripts(@Param("email") email: String, @Param("publication_id") publicationId: Int? = null): List<Manuscript>

    @Modifying
    @Query("INSERT INTO invite (email, target, target_id) VALUES (:email, :target, :target_id)")
    fun insert(@Param("email") email: String, @Param("target") target: InvitationTarget, @Param("target_id") targetId: Int? = null)

    @Modifying
    @Query("DELETE FROM invite WHERE email = :email AND target = :target AND (target_id = :target_id OR :target_id IS NULL)")
    fun revoke(@Param("email") email: String, @Param("target") target: InvitationTarget, @Param("target_id") targetId: Int? = null)
}