package hr.unipu.journals.view.review

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class EicInitialReviewPageController() {
    @GetMapping("/review/{manuscriptId}/eic-initial")
    fun page() = "review/eic-initial-review-page"
}