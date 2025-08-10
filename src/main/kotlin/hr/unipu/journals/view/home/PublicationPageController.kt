package hr.unipu.journals.controller.view.home

import hr.unipu.journals.feature.publication.PublicationRepository
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
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class PublicationPageController(private val repository: PublicationRepository) {

    @GetMapping("/")
    fun findAll(model: Model): String {
        model["publications"] = repository.all()
        return "home/publication-page"
    }
}