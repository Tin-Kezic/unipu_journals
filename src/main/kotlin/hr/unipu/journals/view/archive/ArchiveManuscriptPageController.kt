package hr.unipu.journals.view.archive

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/archive/publication")
class ArchiveManuscriptPageController() {
    @GetMapping("/{publicationId}/section/{sectionId}/")
    fun page(@PathVariable publicationId: Int, @PathVariable sectionId: Int) = "placeholder.html"
}