package hr.unipu.journals.view.hidden

import hr.unipu.journals.feature.manuscript.ManuscriptState
import hr.unipu.journals.feature.publication.PublicationRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HiddenPublicationPageController(private val publicationRepository: PublicationRepository) {
    @GetMapping("/hidden")
    fun page(model: Model): String {
        model["publications"] = publicationRepository.all(ManuscriptState.HIDDEN)
        return "hidden/hidden-publication-page"
    }
}
