package hr.unipu.journals.feature.manuscript.core

import hr.unipu.journals.EmailService
import hr.unipu.journals.feature.publication.core.PublicationRepository
import hr.unipu.journals.feature.section.core.SectionRepository
import hr.unipu.journals.security.AuthorizationService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class ManuscriptService(
    private val manuscriptRepository: ManuscriptRepository,
    private val sectionRepository: SectionRepository,
    private val publicationRepository: PublicationRepository,
    private val authorizationService: AuthorizationService,
    private val emailService: EmailService
) {
    fun updateState(manuscriptId: Int, newState: ManuscriptState): ResponseEntity<String> {
        val manuscript = manuscriptRepository.byId(manuscriptId) ?: return ResponseEntity.badRequest().body("failed to find manuscript")
        val section = sectionRepository.byId(manuscript.sectionId)!!
        val publication = publicationRepository.by(id = section.publicationId)!!
        val isAdmin = authorizationService.isAdmin
        val isSectionEditorOnSectionOrSuperior = authorizationService.isSectionEditorOnSectionOrSuperior(publication.id, section.id)
        val isEditorOnManuscriptOrAffiliatedSuperior = authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscriptId)
        when(newState) {
            ManuscriptState.ARCHIVED -> {
                if(isSectionEditorOnSectionOrSuperior.not()) return ResponseEntity.status(403).body("forbidden to archive manuscripts")
                if(manuscript.state != ManuscriptState.PUBLISHED) return ResponseEntity.badRequest().body("cannot archive manuscript that is not published")
            }
            ManuscriptState.HIDDEN -> {
                if(isAdmin.not()) return ResponseEntity.status(403).body("forbidden to hide manuscripts")
                if(manuscript.state !in setOf(ManuscriptState.PUBLISHED, ManuscriptState.REJECTED)) return ResponseEntity.badRequest().body("cannot hide manuscript that is not published or rejected")
            }
            ManuscriptState.AWAITING_EIC_REVIEW -> return ResponseEntity.badRequest().body("cannot change manuscript state to ${manuscript.state}")
            ManuscriptState.AWAITING_EDITOR_REVIEW -> {
                if(authorizationService.isEicOnManuscript(manuscriptId).not()) return ResponseEntity.status(403).body("forbidden to editor review manuscript")
                if(manuscript.state != ManuscriptState.AWAITING_EIC_REVIEW) return ResponseEntity.badRequest().body("cannot change state to AWAITING_EDITOR_REVIEW from ${manuscript.state}")
            }
            ManuscriptState.AWAITING_ROUND_INITIALIZATION -> {
                if(authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscriptId).not()) return ResponseEntity.status(403).body("forbidden to initialize review round awaiting on manuscript")
                if(manuscript.state !in setOf(ManuscriptState.AWAITING_REVIEWER_REVIEW, ManuscriptState.AWAITING_EDITOR_REVIEW, ManuscriptState.MINOR, ManuscriptState.MAJOR)) return ResponseEntity.badRequest().body("cannot change state to AWAITING_ROUND_INITIALIZATION from ${manuscript.state}")
            }
            ManuscriptState.AWAITING_REVIEWER_REVIEW -> {
                if(isEditorOnManuscriptOrAffiliatedSuperior.not()) return ResponseEntity.status(403).body("forbidden to initialize round on manuscript")
                if(manuscript.state != ManuscriptState.AWAITING_ROUND_INITIALIZATION) return ResponseEntity.badRequest().body("cannot initialize round from ${manuscript.state}")
            }
            ManuscriptState.MINOR, ManuscriptState.MAJOR -> {
                if(isEditorOnManuscriptOrAffiliatedSuperior.not()) return ResponseEntity.status(403).body("forbidden to determine minor/major on manuscript")
                if(manuscript.state !in setOf(ManuscriptState.AWAITING_ROUND_INITIALIZATION, ManuscriptState.AWAITING_REVIEWER_REVIEW)) return ResponseEntity.badRequest().body("cannot initialize round from ${manuscript.state}")
            }
            ManuscriptState.REJECTED -> {
                if(isEditorOnManuscriptOrAffiliatedSuperior.not()) return ResponseEntity.status(403).body("forbidden to reject on manuscript")
                if(manuscript.state.name.contains("AWAITING").not()) return ResponseEntity.badRequest().body("cannot reject manuscript from ${manuscript.state}")
            }
            ManuscriptState.PUBLISHED -> when(manuscript.state) {
                ManuscriptState.HIDDEN -> if(isAdmin.not()) return ResponseEntity.status(403).body("forbidden to unhide manuscripts")
                ManuscriptState.ARCHIVED -> if(isSectionEditorOnSectionOrSuperior.not()) return ResponseEntity.status(403).body("forbidden to unarchive manuscripts")
                ManuscriptState.AWAITING_REVIEWER_REVIEW -> {
                    if(isEditorOnManuscriptOrAffiliatedSuperior.not()) return ResponseEntity.status(403).body("forbidden to publish manuscript")
                }
                else -> return ResponseEntity.badRequest().body("cannot change state to PUBLISHED from ${manuscript.state}")
            }
        }
        val rowsAffected = manuscriptRepository.updateState(manuscriptId, newState)
        return if(rowsAffected == 1) {
            emailService.sendHtml(
                to = manuscript.correspondingAuthorEmail,
                subject = "Manuscript set to ${newState.name.lowercase()}",
                html = "The manuscript ${manuscript.title} has been set to ${newState.name.lowercase()}."
            )
            ResponseEntity.ok("successfully updated state on manuscript")
        }
        else ResponseEntity.internalServerError().body("failed to update state on manuscript")
    }
}