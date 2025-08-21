package hr.unipu.journals.view.archive

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/archive/publication")
class ArchiveSectionPageController() {
    @GetMapping("/{publicationId}")
    fun page(@PathVariable publicationId: Int) = "placeholder.html"
}