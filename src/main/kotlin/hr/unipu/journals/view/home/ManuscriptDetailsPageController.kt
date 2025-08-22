package hr.unipu.journals.view.home

import hr.unipu.journals.feature.manuscript.ManuscriptRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/publication")
class ManuscriptDetailsPageController(private val manuscriptRepository: ManuscriptRepository) {
    @GetMapping("/{publicationId}/section/{sectionId}/manuscript/{manuscriptId}")
    fun page(@PathVariable manuscriptId: Int, model: Model): String {
        model["manuscript"] = manuscriptRepository.byId(manuscriptId)
        return "home/manuscript-page"
    }
}