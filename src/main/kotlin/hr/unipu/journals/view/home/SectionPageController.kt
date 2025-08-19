package hr.unipu.journals.view.home

import hr.unipu.journals.feature.publication.PublicationRepository
import hr.unipu.journals.feature.section.SectionRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/publication")
class SectionPageController(
    private val publicationRepository: PublicationRepository,
    private val sectionRepository: SectionRepository,
) {
    @GetMapping("/{publicationId}")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ROOT)
    fun page(@PathVariable publicationId: Int, model: Model): String {
        model["publications-sidebar"] = publicationRepository.allPublished()
        model["sections"] = sectionRepository.allByPublicationId(publicationId)
        return "home/section-page"
    }
}