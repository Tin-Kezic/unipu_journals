package hr.unipu.journals.view.hidden

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class HiddenManuscriptPageController() {
    @GetMapping("/hidden/publication/{publicationId}/section/{sectionId}/")
    fun page(@PathVariable publicationId: Int, @PathVariable sectionId: Int) = "placeholder.html"
}
