package hr.unipu.journals.controller.view.home

import hr.unipu.journals.feature.publication.PublicationRepository
import hr.unipu.journals.feature.section.PublicationSectionRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/publication/{id}")
class SectionPageController(
    private val publicationRepository: PublicationRepository,
    private val publicationSectionRepository: PublicationSectionRepository
) {

    @GetMapping("/")
    fun findAll(model: Model): String {
        model["publications"] = publicationRepository.all()
        model["sections"] = publicationSectionRepository.all()
        return "home/section-page"
    }
}