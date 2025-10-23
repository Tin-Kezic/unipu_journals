package hr.unipu.journals.feature.category

import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_ADMIN
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
    fun insert(@RequestParam name: String) = categoryRepository.insert(name)

    @DeleteMapping
    fun delete(@RequestParam name: String) = categoryRepository.delete(name)
}