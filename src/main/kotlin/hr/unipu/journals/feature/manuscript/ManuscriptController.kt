package hr.unipu.journals.feature.manuscript

import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_SECTION_EDITOR_ON_SECTION_OR_SUPERIOR
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
class ManuscriptController(private val publicationRepository: ManuscriptRepository) {
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
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_SECTION_EDITOR_ON_SECTION_OR_SUPERIOR)
    fun updateState(
        @PathVariable publicationId: Int,
        @PathVariable sectionId: Int,
        @PathVariable manuscriptId: Int,
        @RequestParam newState: ManuscriptState
    ): ResponseEntity<String> {
        return if(publicationRepository.exists(manuscriptId)) {
            publicationRepository.updateState(manuscriptId, newState)
            return ResponseEntity.ok("manuscript isHidden successfully updated")
        } else ResponseEntity.badRequest().body("manuscript with id: $manuscriptId does not exist")
    }
}