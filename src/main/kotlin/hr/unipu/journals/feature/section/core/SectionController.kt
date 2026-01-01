package hr.unipu.journals.feature.section.core

import hr.unipu.journals.feature.manuscript.core.ManuscriptStateFilter
import hr.unipu.journals.feature.publication.core.Role
import hr.unipu.journals.feature.publication.core.Sorting
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_ADMIN
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_EIC_ON_PUBLICATION_OR_ADMIN
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_SECTION_EDITOR_ON_SECTION_OR_SUPERIOR
import hr.unipu.journals.security.AuthorizationService
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/publications")
class SectionController(
    private val sectionRepository: SectionRepository,
    private val authorizationService: AuthorizationService
) {
    @GetMapping("/{publicationTitle}/sections/titles")
    fun sectionTitles(@PathVariable publicationTitle: String): List<String> {
        return sectionRepository.allPublishedTitlesByPublicationTitle(publicationTitle)
    }
    @GetMapping("/{publicationId}/sections")
    fun all(
        @PathVariable publicationId: Int,
        @RequestParam manuscriptStateFilter: ManuscriptStateFilter,
        @RequestParam role: Role?,
        @RequestParam category: String?,
        @RequestParam sorting: Sorting?,
        @RequestParam from: String?,
        @RequestParam to: String?
    ): List<Map<String, Any>> {
        val isAdmin = authorizationService.isAdmin
        return sectionRepository.all(
            publicationId = publicationId,
            manuscriptStateFilter = manuscriptStateFilter,
            role = role,
            accountId = authorizationService.account?.id,
            category = category,
            sorting = sorting,
            from = from,
            to = to
        ).map { section -> buildMap {
            put("id", section.id)
            put("title", section.title)
            put("description", section.description!!)
            if(authorizationService.isEicOnPublicationOrAdmin(publicationId)) put("role", "EIC")
            else if(authorizationService.isSectionEditorOnSectionOrSuperior(publicationId, section.id)) put("role", "SECTION_EDITOR")
            put("isHidden", section.isHidden)
        }}
    }
    @PostMapping("/{publicationId}/sections")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_EIC_ON_PUBLICATION_OR_ADMIN)
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
            if(rowsAffected == 1) ResponseEntity.ok("section successfully added")
            else ResponseEntity.internalServerError().body("failed to add section")
        } catch (_: DataIntegrityViolationException) { ResponseEntity.badRequest().body("section with title $title already exists") }
    }
    @PutMapping("/{publicationId}/sections/{sectionId}")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_SECTION_EDITOR_ON_SECTION_OR_SUPERIOR)
    fun update(
        @PathVariable publicationId: Int,
        @PathVariable sectionId: Int,
        @RequestParam title: String?,
        @RequestParam description: String?,
        @RequestParam isHidden: Boolean?,
    ): ResponseEntity<String> {
        require(isHidden == null || authorizationService.isAdmin)
        return try {
            val rowsAffected = sectionRepository.update(
                id = sectionId,
                title = title?.run { Jsoup.clean(this, Safelist.none()) },
                description = description?.run { Jsoup.clean(this, Safelist.relaxed()) },
                isHidden = isHidden
            )
            if (rowsAffected == 1) ResponseEntity.ok("section successfully updated")
            else ResponseEntity.internalServerError().body("failed to update section")
        } catch (_: DataIntegrityViolationException) { ResponseEntity.badRequest().body("section with title $title already exists") }
    }
    @DeleteMapping("/{publicationId}/sections/{sectionId}")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ADMIN)
    fun delete(@PathVariable sectionId: Int): ResponseEntity<String> {
        sectionRepository.delete(sectionId)
        return ResponseEntity.ok("successfully deleted section $sectionId")
    }
}