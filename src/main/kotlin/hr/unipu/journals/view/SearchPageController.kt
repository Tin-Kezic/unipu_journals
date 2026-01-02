package hr.unipu.journals.view

import hr.unipu.journals.feature.manuscript.category.CategoryRepository
import hr.unipu.journals.security.AuthorizationService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class SearchPageController(
    private val categoryRepository: CategoryRepository,
    private val authorizationService: AuthorizationService
) {
    @GetMapping("/search")
    fun page(@RequestParam query: String?, model: Model): String {
        model["isAuthenticated"] = authorizationService.isAuthenticated
        model["isAdmin"] = authorizationService.isAdmin
        model["categories"] = categoryRepository.all()
        return "search-page"
    }
}