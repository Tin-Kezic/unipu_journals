package hr.unipu.journals.view.submit

import hr.unipu.journals.feature.manuscript.category.CategoryRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ManageCategoryPageController(private val categoryRepository: CategoryRepository) {
    @GetMapping("/category")
    fun page(model: Model): String {
        model["categories"] = categoryRepository.all()
        return "submit/manage-category-page"
    }
}