package hr.unipu.journals.view.review
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ReviewPageController() {
    @GetMapping("/review/manuscript/{manuscriptReviewId}")
    fun page() = "review/review-page"
}
