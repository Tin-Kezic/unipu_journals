package hr.unipu.journals.view.review

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class PendingReviewPageController() {
    @GetMapping("/review")
    fun page() = "review/pending-review-page"
}