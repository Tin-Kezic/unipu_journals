package hr.unipu.journals.feature.section.core

import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_EIC_ON_PUBLICATION_OR_SUPERIOR
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_SECTION_EDITOR_ON_SECTION_OR_SUPERIOR
import hr.unipu.journals.security.AuthorizationService
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/publication")
class SectionController(
    private val sectionRepository: SectionRepository,
    private val authorizationService: AuthorizationService
) {

    @GetMapping("/{publicationTitle}/section/titles")
    fun sectionTitles(@PathVariable publicationTitle: String): List<String> {
        return sectionRepository.allPublishedTitlesByPublicationTitle(publicationTitle)
    }
    @PostMapping("/{publicationId}/section")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_EIC_ON_PUBLICATION_OR_SUPERIOR)
    fun insert(
        @PathVariable publicationId: Int,
        @RequestParam title: String,
    ): ResponseEntity<String> {
        if(title.isEmpty()) return ResponseEntity.badRequest().body("title must not be empty")
        return try {
            val rowsAffected = sectionRepository.insert(
                title = Jsoup.clean(title, Safelist.none()),
                publicationId = publicationId,
            )
            if(rowsAffected == 1) ResponseEntity.ok("account successfully added")
            else ResponseEntity.internalServerError().body("failed to add section")
        } catch (_: DataIntegrityViolationException) { ResponseEntity.badRequest().body("section with title $title already exists") }
    }
    @PutMapping("/{publicationId}/section/{sectionId}")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_SECTION_EDITOR_ON_SECTION_OR_SUPERIOR)
    fun update(
        @PathVariable sectionId: Int,
        @RequestParam title: String?,
        @RequestParam description: String?,
        @RequestParam isHidden: Boolean?,
    ): ResponseEntity<String> {
        if(isHidden != null && authorizationService.isAdmin().not()) return ResponseEntity.badRequest().body("unauthorized to change if section $sectionId is hidden")
        return try {
            val rowsAffected = sectionRepository.update(
                id = sectionId,
                title = title,
                description = description,
                isHidden = isHidden
            )
            if (rowsAffected == 1) ResponseEntity.ok("section successfully updated")
            else ResponseEntity.internalServerError().body("failed to update section")
        } catch (_: DataIntegrityViolationException) { ResponseEntity.badRequest().body("section with title $title already exists") }
    }
}