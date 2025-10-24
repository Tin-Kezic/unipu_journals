package hr.unipu.journals.view.hidden

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class HiddenSectionPageController() {
    @GetMapping("/hidden/publication/{publicationId}")
    fun page(@PathVariable publicationId: Int) = "placeholder.html"
}
