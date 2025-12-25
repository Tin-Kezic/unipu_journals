package hr.unipu.journals.view

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class SearchPageController {
    @GetMapping("/search")
    fun page(): String {
        return "search-page"
    }
}