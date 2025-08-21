package hr.unipu.journals.view.archive

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ArchivePublicationPageController() {
    @GetMapping("/archive")
    fun page() = "placeholder.html"
}