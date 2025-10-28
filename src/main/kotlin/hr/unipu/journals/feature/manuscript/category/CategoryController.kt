package hr.unipu.journals.feature.manuscript.category

import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_ADMIN
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/category")
@PreAuthorize(AUTHORIZATION_SERVICE_IS_ADMIN)
class CategoryController(private val categoryRepository: CategoryRepository) {
    @PostMapping
    fun insert(@RequestParam name: String) = categoryRepository.insert(Jsoup.clean(name, Safelist.none()))

    @DeleteMapping
    fun delete(@RequestParam name: String) = categoryRepository.delete(Jsoup.clean(name, Safelist.none()))
}