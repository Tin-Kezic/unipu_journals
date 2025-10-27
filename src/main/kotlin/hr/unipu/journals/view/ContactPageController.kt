package hr.unipu.journals.view

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

private var description = ""

@Controller
class ContactPageController() {
    @GetMapping("/contact")
    fun page() = "contact"
}