package hr.unipu.journals.view.review

import hr.unipu.journals.feature.manuscript.ManuscriptRepository
import hr.unipu.journals.feature.publication.PublicationRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/review")
class PendingReviewPageController(
    private val publicationRepository: PublicationRepository,
    private val manuscriptRepository: ManuscriptRepository
) {
    @GetMapping("/")
    fun all(model: Model): String {
        model["publications-sidebar"] = publicationRepository.allUnderReview()
        model["manuscripts"] = manuscriptRepository.allUnderReview()
        return "home/section-page"
    }
    @GetMapping("/from-publication/{publicationId}")
    fun underPublication(@PathVariable publicationId: Int, model: Model): String {
        model["publications-sidebar"] = publicationRepository.allPublished()
        model["sections"] = manuscriptRepository.allUnderReviewByPublication(publicationId)
        return "home/section-page"
    }
}