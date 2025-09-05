package hr.unipu.journals.view.hidden

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/hidden/publication")
class HiddenManuscriptPageController() {
    @GetMapping("/{publicationId}/section/{sectionId}/")
    fun page(@PathVariable publicationId: Int, @PathVariable sectionId: Int) = "placeholder.html"
}
