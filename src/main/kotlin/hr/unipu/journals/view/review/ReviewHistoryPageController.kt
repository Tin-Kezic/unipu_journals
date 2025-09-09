package hr.unipu.journals.view.review

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ReviewHistoryPageController() {
    @GetMapping("/publication/{publicationId}/section/{sectionId}/manuscript/{manuscriptId}/review-history")
    fun page() = "review/review-history"
}
