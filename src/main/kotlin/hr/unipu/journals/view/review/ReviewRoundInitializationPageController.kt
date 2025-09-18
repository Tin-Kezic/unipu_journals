package hr.unipu.journals.view.review

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ReviewRoundInitializationPageController() {
    @GetMapping("/review/manuscript/{manuscriptReviewId}/round-initialization")
    fun page() = "manage/manage-round-initialization-page"
}