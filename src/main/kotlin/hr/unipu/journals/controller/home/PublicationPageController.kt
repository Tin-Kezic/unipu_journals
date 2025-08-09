package hr.unipu.journals.controller.home

import hr.unipu.journals.data.entity.Publication
import hr.unipu.journals.data.repository.PublicationRepository
import hr.unipu.journals.usecase.sanitize
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class PublicationPageController(private val repository: PublicationRepository) {

    @GetMapping("/")
    fun findAll(model: Model): String {
        model["publications"] = repository.all()
        return "home/publication-page"
    }

    @ResponseBody
    @PostMapping("/api/publication/insert")
    fun insert(@ModelAttribute title: String): ResponseEntity<String> {
        return try {
            repository.insertPublication(sanitize(title))
            ResponseEntity.ok().body("account successfully added")
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().body("Invalid account data. title must be non-null")
        } catch (_: OptimisticLockingFailureException) {
            ResponseEntity.internalServerError().body("internal server error of type OptimisticLockingFailureException")
        }
    }
    @ResponseBody
    @PostMapping("/api/publication/hide/{id}")
    fun hidePublication(@PathVariable id: Int): ResponseEntity<String> {
        return if (repository.existsById(id)) {
            repository.hidePublication(id)
            ResponseEntity.ok().body("publication successfully hidden")
        } else ResponseEntity.badRequest().body("id does not exist")
    }
}