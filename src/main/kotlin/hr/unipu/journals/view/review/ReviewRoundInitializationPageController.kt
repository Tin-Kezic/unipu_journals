package hr.unipu.journals.view.review

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ReviewRoundInitializationPageController() {
    @GetMapping("/manuscripts/{manuscriptId}/round-initialization")
    fun page() = "review/round-initialization-page"
}