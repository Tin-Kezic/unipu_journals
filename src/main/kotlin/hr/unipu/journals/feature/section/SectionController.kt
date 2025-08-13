package hr.unipu.journals.feature.section

import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/publication/")
class SectionController(private val repository: SectionRepository) {

    @PostMapping("{publicationId}/insert")
    fun insert(
        @PathVariable publicationId: Int,
        @ModelAttribute title: String,
        @ModelAttribute description: String
    ): ResponseEntity<String> {
        return if(title.isNotEmpty()) {
            repository.insert(
                title = Jsoup.clean(title, Safelist.none()),
                description = Jsoup.clean(description, Safelist.relaxed()),
                publicationId = publicationId,
            )
            ResponseEntity.ok().body("account successfully added")
        } else ResponseEntity.badRequest().body("title must not be empty")
    }
    @PutMapping("{publicationId}/update-title-and-description")
    fun update(
        @PathVariable sectionId: Int,
        @ModelAttribute title: String,
        @ModelAttribute description: String
    ): ResponseEntity<String> {
        return if (repository.existsById(sectionId)) {
            repository.updateTitleAndDescription(sectionId, title, description)
            ResponseEntity.ok().body("title successfully updated")
        } else ResponseEntity.badRequest().body("section with id: $sectionId does not exist")
    }
    @PutMapping("{publicationId}/hide/{section_id}")
    fun hidePublication(
        @PathVariable sectionId: Int,
        @RequestParam isHidden: Boolean
    ): ResponseEntity<String> {
        return if (repository.existsById(sectionId)) {
            repository.updateHidden(sectionId, isHidden)
            ResponseEntity.ok().body("publication successfully hidden")
        } else ResponseEntity.badRequest().body("id does not exist")
    }
}