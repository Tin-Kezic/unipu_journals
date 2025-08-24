package hr.unipu.journals.view.contact

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping


@Controller
class ContactPageController() {
    @GetMapping("/contact")
    fun page() = "placeholder.html"
}