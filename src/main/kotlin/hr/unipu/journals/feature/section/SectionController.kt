package hr.unipu.journals.feature.section

import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/publication")
class SectionController(private val sectionRepository: SectionRepository) {

    @GetMapping("/{publicationTitle}/section/titles")
    fun sectionTitles(@PathVariable publicationTitle: String): List<String> {
        return sectionRepository.allPublishedTitlesByPublicationTitle(publicationTitle)
    }
    @PostMapping("/{publicationId}/section")
    fun insert(
        @PathVariable publicationId: Int,
        @RequestParam title: String,
    ): ResponseEntity<String> {
        if(title.isEmpty()) return ResponseEntity.badRequest().body("title must not be empty")
        val rowsAffected = sectionRepository.insert(
            title = Jsoup.clean(title, Safelist.none()),
            publicationId = publicationId,
        )
        return if(rowsAffected == 1) ResponseEntity.ok("account successfully added")
        else ResponseEntity.internalServerError().body("failed to add section")
    }
    @PutMapping("/{publicationId}/section/{sectionId}")
    fun update(
        @PathVariable sectionId: Int,
        @RequestParam title: String?,
        @RequestParam description: String?,
        @RequestParam isHidden: Boolean?,
    ): ResponseEntity<String> {
        val rowsAffected = sectionRepository.update(
            id = sectionId,
            title = title,
            description = description,
            isHidden = isHidden
        )
        return if(rowsAffected == 1) ResponseEntity.ok("section successfully updated")
        else ResponseEntity.internalServerError().body("failed to update section")
    }
}