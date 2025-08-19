package hr.unipu.journals.view

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class RootPageController() {
    @GetMapping("/root")
    fun page() = "configure/root-page"
}
