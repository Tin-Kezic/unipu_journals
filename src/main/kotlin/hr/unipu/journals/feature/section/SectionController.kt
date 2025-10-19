package hr.unipu.journals.feature.section

import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
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

    @GetMapping("/{publicationTitle}/section-titles")
    fun sectionTitles(@PathVariable publicationTitle: String): List<String> {
        return sectionRepository.titlesByPublicationTitle(publicationTitle)
    }
    @PostMapping("/{publicationId}/section/insert")
    fun insert(
        @PathVariable publicationId: Int,
        @RequestParam title: String,
    ): ResponseEntity<String> {
        return if(title.isNotEmpty()) {
            sectionRepository.insert(
                title = Jsoup.clean(title, Safelist.none()),
                publicationId = publicationId,
            )
            ResponseEntity.ok("account successfully added")
        } else ResponseEntity.badRequest().body("title must not be empty")
    }
    @PutMapping("/{publicationId}/section/{sectionId}/update-title")
    fun updateTitle(
        @PathVariable sectionId: Int,
        @RequestParam title: String,
    ): ResponseEntity<String> {
        return if (sectionRepository.exists(sectionId)) {
            sectionRepository.updateTitle(sectionId, title)
            ResponseEntity.ok("title successfully updated")
        } else ResponseEntity.badRequest().body("section with id $sectionId does not exist")
    }
    @PutMapping("/{publicationId}/section/{sectionId}/update-description")
    fun updateDescription(
        @PathVariable sectionId: Int,
        @RequestParam description: String,
    ): ResponseEntity<String> {
        return if (sectionRepository.exists(sectionId)) {
            sectionRepository.updateDescription(sectionId, description)
            ResponseEntity.ok("description successfully updated")
        } else ResponseEntity.badRequest().body("section with id $sectionId does not exist")
    }
    @PutMapping("/{publicationId}/section/{sectionId}/update-hidden")
    fun updateHidden(
        @PathVariable sectionId: Int,
        @RequestParam isHidden: Boolean
    ): ResponseEntity<String> {
        return if (sectionRepository.exists(sectionId)) {
            sectionRepository.updateHidden(sectionId, isHidden)
            ResponseEntity.ok("publication successfully hidden")
        } else ResponseEntity.badRequest().body("section with id $sectionId does not exist")
    }
}