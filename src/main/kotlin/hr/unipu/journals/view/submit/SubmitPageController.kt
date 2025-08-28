package hr.unipu.journals.view.submit

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class SubmitPageController() {
    @GetMapping("/submit")
    fun page() = "submit/submit-page"
}
