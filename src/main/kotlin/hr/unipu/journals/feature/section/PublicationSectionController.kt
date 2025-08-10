package hr.unipu.journals.controller.old

import hr.unipu.journals.feature.section.PublicationSectionRepository
import hr.unipu.journals.usecase.sanitize
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/publication/{id}")
class PublicationSectionController(private val repository: PublicationSectionRepository) {

    @PostMapping("/insert")
    fun insert(@ModelAttribute title: String): ResponseEntity<String> {
        return try {
            repository.insert(sanitize(title))
            ResponseEntity.ok().body("account successfully added")
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().body("Invalid account data. title must be non-null")
        } catch (_: OptimisticLockingFailureException) {
            ResponseEntity.internalServerError().body("internal server error of type OptimisticLockingFailureException")
        }
    }

    @PutMapping("/update")
    fun update(@ModelAttribute id: Int, @ModelAttribute title: String): ResponseEntity<String> {
        if(!repository.existsById(id)) return ResponseEntity.badRequest().body("publication with id: $id does not exist")
        repository.update(id, title)
        return ResponseEntity.ok().body("title successfully updated")
    }

    @PutMapping("/hide/{id}")
    fun hidePublication(@PathVariable id: Int): ResponseEntity<String> {
        return if (repository.existsById(id)) {
            repository.hide(id)
            ResponseEntity.ok().body("publication successfully hidden")
        } else ResponseEntity.badRequest().body("id does not exist")
    }
}