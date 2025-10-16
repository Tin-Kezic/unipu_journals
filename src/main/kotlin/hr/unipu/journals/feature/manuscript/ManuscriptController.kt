package hr.unipu.journals.feature.manuscript

import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_SECTION_EDITOR_ON_SECTION_OR_SUPERIOR
import hr.unipu.journals.security.AuthorizationService
import hr.unipu.journals.view.submit.AuthorDTO
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/publication")
class ManuscriptController(
    private val manuscriptRepository: ManuscriptRepository,
    private val authorizationService: AuthorizationService
) {
    @PostMapping("{publicationId}/section/{sectionId}/insert")
    fun insert(
        @PathVariable sectionId: Int,
        @RequestParam title: String,
        @RequestParam category: String,
        @RequestParam authors: List<AuthorDTO>,
        @RequestParam abstract: String,
        @RequestParam files: List<MultipartFile>,

    ): ResponseEntity<String> {
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
    @PutMapping("/{publicationId}/section/{sectionId}/manuscript/{manuscriptId}/update-state")
    fun updateState(
        @PathVariable publicationId: Int,
        @PathVariable sectionId: Int,
        @PathVariable manuscriptId: Int,
        @RequestParam newState: ManuscriptState
    ): ResponseEntity<String> {
        val manuscript = manuscriptRepository.byId(manuscriptId) ?: return ResponseEntity.badRequest().body("failed to find manuscript with id $manuscriptId")
        if(newState !in listOf(ManuscriptState.HIDDEN, ManuscriptState.ARCHIVED)) return ResponseEntity.badRequest().body("cannot manually change manuscript state to $newState")
        if(newState == ManuscriptState.ARCHIVED) {
            if(authorizationService.isSectionEditorOnSectionOrSuperior(publicationId, sectionId)) return ResponseEntity.status(403).body("unauthorized to archive manuscripts in section $sectionId")
            if(manuscript.state != ManuscriptState.PUBLISHED) return ResponseEntity.badRequest().body("cannot archive manuscript that is not published")
        }
        if(newState == ManuscriptState.HIDDEN) {
            if(authorizationService.isAdmin()) return ResponseEntity.status(403).body("unauthorized to hide manuscripts")
            if(manuscript.state !in listOf(ManuscriptState.PUBLISHED, ManuscriptState.REJECTED)) return ResponseEntity.badRequest().body("cannot hide manuscript that is not published or rejected")
        }
        val rowsAffected = manuscriptRepository.updateState(manuscriptId, newState)
        return if(rowsAffected == 1) ResponseEntity.ok("successfully updated state on manuscript: $manuscriptId")
        else ResponseEntity.internalServerError().body("failed updating state on manuscript: $manuscriptId")
    }
}