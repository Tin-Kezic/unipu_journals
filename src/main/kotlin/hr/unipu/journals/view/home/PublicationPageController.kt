package hr.unipu.journals.view.home

import hr.unipu.journals.feature.publication.PublicationRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping

@Controller
class PublicationPageController(private val publicationRepository: PublicationRepository) {
    @GetMapping("/")
    fun page(model: Model): String {
        model["publications"] = publicationRepository.allPublished()
        return "home/publication-page"
    }
}