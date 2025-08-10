package hr.unipu.journals.feature.section

import hr.unipu.journals.usecase.sanitize
import org.jsoup.safety.Safelist
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
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
        return try {
            repository.insert(
                title = sanitize(title),
                description = sanitize(description),
                publicationId = publicationId,
            )
            ResponseEntity.ok().body("account successfully added")
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().body("Invalid account data. title must be non-null")
        } catch (_: OptimisticLockingFailureException) {
            ResponseEntity.internalServerError().body("internal server error of type OptimisticLockingFailureException")
        }
    }
    @PutMapping("{publicationId}/updateTitle")
    fun update(
        @PathVariable publicationId: Int,
        @PathVariable sectionId: Int,
        @ModelAttribute title: String
    ): ResponseEntity<String> {
        return if (repository.existsById(sectionId)) {
            repository.updateTitle(sectionId, title)
            ResponseEntity.ok().body("title successfully updated")
        } else ResponseEntity.badRequest().body("section with id: $sectionId does not exist")
    }
    @PutMapping("{publicationId}/hide/{section_id}")
    fun hidePublication(
        @PathVariable publicationId: Int,
        @PathVariable sectionId: Int,
    ): ResponseEntity<String> {
        return if (repository.existsById(sectionId)) {
            repository.hide(sectionId)
            ResponseEntity.ok().body("publication successfully hidden")
        } else ResponseEntity.badRequest().body("id does not exist")
    }
}