package hr.unipu.journals.view.review
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/review")
class ReviewPageController() {
    @GetMapping("/manuscript/{manuscriptReviewId}")
    fun page() = "review/review-page"
}
