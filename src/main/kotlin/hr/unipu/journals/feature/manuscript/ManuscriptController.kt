package hr.unipu.journals.feature.manuscript

import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_SECTION_EDITOR_ON_SECTION_OR_SUPERIOR
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

@RestController
@RequestMapping("/api/publication")
class ManuscriptController(private val publicationRepository: ManuscriptRepository) {
    @PostMapping("{publicationId}/section/{sectionId}/insert")
    fun insert(
        @PathVariable sectionId: Int,
        @RequestParam manuscript: InsertManuscriptDTO
    ): ResponseEntity<String> {
        publicationRepository.insert(
            title = Jsoup.clean(manuscript.title, Safelist.none()),
            authorId = manuscript.authorId,
            categoryId = manuscript.categoryId,
            sectionId = sectionId,
            fileUrl = Jsoup.clean(manuscript.fileUrl, Safelist.none())
        )
        return ResponseEntity.ok("manuscript successfully added")
    }
    @PutMapping("/{publicationId}/section/{sectionId}/manuscript/{manuscriptId}/update-hidden")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_SECTION_EDITOR_ON_SECTION_OR_SUPERIOR)
    fun updateHidden(
        @PathVariable publicationId: Int,
        @PathVariable sectionId: Int,
        @PathVariable manuscriptId: Int,
        @RequestParam isHidden: Boolean
    ): ResponseEntity<String> {
        return if(publicationRepository.exists(manuscriptId)) {
            if(isHidden) publicationRepository.hide(manuscriptId)
            else publicationRepository.publish(manuscriptId)
            return ResponseEntity.ok("manuscript isHidden successfully updated")
        } else ResponseEntity.badRequest().body("manuscript with id: $manuscriptId does not exist")
    }
    @PutMapping("/{publicationId}/section/{sectionId}/manuscript/{manuscriptId}/update-archived")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_SECTION_EDITOR_ON_SECTION_OR_SUPERIOR)
    fun updateArchived(
        @PathVariable publicationId: Int,
        @PathVariable sectionId: Int,
        @PathVariable manuscriptId: Int,
        @RequestParam isArchived: Boolean
    ): ResponseEntity<String> {
        return if(publicationRepository.exists(manuscriptId)) {
            if(isArchived) publicationRepository.archive(manuscriptId)
            else publicationRepository.publish(manuscriptId)
            return ResponseEntity.ok("manuscript isArchived successfully updated")
        } else ResponseEntity.badRequest().body("manuscript with id: $manuscriptId does not exist")
    }
}