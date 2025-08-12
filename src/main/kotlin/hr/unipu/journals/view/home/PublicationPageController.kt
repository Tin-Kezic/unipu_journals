package hr.unipu.journals.view.home

import hr.unipu.journals.feature.publication.PublicationRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping

@Controller
class PublicationPageController(private val repository: PublicationRepository) {
    @GetMapping("/")
    fun findAll(model: Model): String {
        model["publications"] = repository.allPublished()
        return "home/publication-page"
    }
}