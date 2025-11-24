package hr.unipu.journals.feature.manuscript.core

import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_AUTHENTICATED
import hr.unipu.journals.security.AuthorizationService
import hr.unipu.journals.view.home.ManuscriptDTO
import hr.unipu.journals.view.submit.AuthorDTO
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/publication/{publicationId}/section/{sectionId}")
class ManuscriptController(
    private val manuscriptRepository: ManuscriptRepository,
    private val authorizationService: AuthorizationService,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository
) {
    @GetMapping("/manuscripts")
    fun all(@PathVariable sectionId: Int): List<ManuscriptDTO> {
        return manuscriptRepository.allBySectionId(sectionId).map { manuscript ->
            ManuscriptDTO(
                id = manuscript.id,
                title = manuscript.title,
                authors = accountRoleOnManuscriptRepository.authors(manuscript.id),
                downloadUrl = manuscript.downloadUrl,
                submissionDate = manuscript.submissionDate.format(DateTimeFormatter.ofPattern("dd MMM YYYY")) ?: "no publication date",
                publicationDate = manuscript.publicationDate?.format(DateTimeFormatter.ofPattern("dd MMM YYYY")) ?: "no publication date",
                description = manuscript.description
            )
        }
    }
    @PostMapping
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_AUTHENTICATED)
    fun insert(
        @PathVariable sectionId: Int,
        @RequestParam title: String,
        @RequestParam category: String,
        @RequestParam authors: List<AuthorDTO>,
        @RequestParam abstract: String,
        @RequestParam files: List<MultipartFile>,
    ): ResponseEntity<String> {
        // clean inputs with Jsoup
        /*
        publicationRepository.insert(
            title = Jsoup.clean(title, Safelist.none()),
            category = Jsoup.clean(category, Safelist.none()),
            authors = authors,
            abstract = abstract,
            files = files
        )
         */
        return ResponseEntity.ok("manuscript successfully added")
    }
    @PutMapping("/manuscript/{manuscriptId}/state")
    fun updateState(
        @PathVariable publicationId: Int,
        @PathVariable sectionId: Int,
        @PathVariable manuscriptId: Int,
        @RequestParam newState: ManuscriptState
    ): ResponseEntity<String> {
        val manuscript = manuscriptRepository.byId(manuscriptId) ?: return ResponseEntity.badRequest().body("failed to find manuscript $manuscriptId")
        val isAdmin = authorizationService.isAdmin
        val isSectionEditorOnSectionOrSuperior = authorizationService.isSectionEditorOnSectionOrSuperior(publicationId, sectionId)
        val isEditorOnManuscriptOrAffiliatedSuperior = authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscriptId)
        when(newState) {
            ManuscriptState.ARCHIVED -> {
                if(isSectionEditorOnSectionOrSuperior) return ResponseEntity.status(403).body("unauthorized to archive manuscripts in section $sectionId")
                if(manuscript.state != ManuscriptState.PUBLISHED) return ResponseEntity.badRequest().body("cannot archive manuscript that is not published")
            }
            ManuscriptState.HIDDEN -> {
                if(isAdmin) return ResponseEntity.status(403).body("unauthorized to hide manuscripts")
                if(manuscript.state !in listOf(ManuscriptState.PUBLISHED, ManuscriptState.REJECTED)) return ResponseEntity.badRequest().body("cannot hide manuscript that is not published or rejected")
            }
            ManuscriptState.AWAITING_EIC_REVIEW -> return ResponseEntity.badRequest().body("cannot change manuscript state to $newState")
            ManuscriptState.AWAITING_EDITOR_REVIEW -> {
                if(authorizationService.isEicOnManuscript(manuscriptId).not()) return ResponseEntity.status(403).body("unauthorized to EiC review manuscripts $manuscriptId")
                if(manuscript.state != ManuscriptState.AWAITING_EIC_REVIEW) return ResponseEntity.badRequest().body("cannot change state to AWAITING_EDITOR_REVIEW from $newState")
            }
            ManuscriptState.AWAITING_REVIEWER_REVIEW -> {
                if(isEditorOnManuscriptOrAffiliatedSuperior) return ResponseEntity.status(403).body("unauthorized to initialize round on manuscript $manuscriptId")
                if(manuscript.state != ManuscriptState.AWAITING_EDITOR_REVIEW) return ResponseEntity.badRequest().body("cannot initialize round from $newState")
            }
            ManuscriptState.MINOR, ManuscriptState.MAJOR, ManuscriptState.REJECTED -> {
                if(isEditorOnManuscriptOrAffiliatedSuperior) return ResponseEntity.status(403).body("unauthorized to determine minor on manuscript $manuscriptId")
            }
            ManuscriptState.PUBLISHED -> when(manuscript.state) {
                ManuscriptState.HIDDEN -> if(isAdmin.not()) return ResponseEntity.status(403).body("unauthorized to unhide manuscripts")
                ManuscriptState.ARCHIVED -> if(isSectionEditorOnSectionOrSuperior.not()) return ResponseEntity.status(403).body("unauthorized to unarchive manuscripts in section $sectionId")
                ManuscriptState.AWAITING_REVIEWER_REVIEW -> {
                    if(isEditorOnManuscriptOrAffiliatedSuperior) return ResponseEntity.status(403).body("unauthorized to determine minor on manuscript $manuscriptId")
                }
                else -> return ResponseEntity.badRequest().body("cannot change state to PUBLISHED from $newState")
            }
        }
        val rowsAffected = manuscriptRepository.updateState(manuscriptId, newState)
        return if(rowsAffected == 1) ResponseEntity.ok("successfully updated state on manuscript: $manuscriptId")
        else ResponseEntity.internalServerError().body("failed to update state on manuscript: $manuscriptId")
    }
}