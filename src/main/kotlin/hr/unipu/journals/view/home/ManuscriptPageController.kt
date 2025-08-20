package hr.unipu.journals.view.home

import hr.unipu.journals.feature.manuscript.ManuscriptRepository
import hr.unipu.journals.feature.section.SectionRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/publication")
class ManuscriptPageController(
    private val sectionRepository: SectionRepository,
    private val manuscriptRepository: ManuscriptRepository
) {
    @GetMapping("/{publicationId}/section/{sectionId}")
    fun page(
        @PathVariable publicationId: Int,
        @PathVariable sectionId: Int,
        model: Model
    ): String {
        model["sections-sidebar"] = sectionRepository.allPublishedByPublicationId(publicationId)
        model["manuscripts"] = manuscriptRepository.allPublishedBySectionId(sectionId)
        model["title"] = sectionRepository.titleById(sectionId)
        return "home/manuscript-page"
    }
}