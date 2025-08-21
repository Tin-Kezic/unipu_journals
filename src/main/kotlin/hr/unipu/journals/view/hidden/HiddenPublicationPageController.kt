package hr.unipu.journals.view.hidden

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HiddenPublicationPageController() {
    @GetMapping("/hidden")
    fun page() = "placeholder.html"
}
