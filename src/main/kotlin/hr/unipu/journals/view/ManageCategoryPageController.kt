package hr.unipu.journals.view

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ManageCategoryPageController() {
    @GetMapping("/category")
    fun page() = "manage/manage-category"
}
