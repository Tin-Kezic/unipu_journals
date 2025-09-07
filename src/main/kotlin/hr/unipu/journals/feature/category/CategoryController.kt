package hr.unipu.journals.feature.category

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/category")
class CategoryController(private val categoryRepository: CategoryRepository) {
    @PostMapping("/insert")
    fun insert(@RequestParam category: String) = categoryRepository.insert(category)

    @DeleteMapping("/delete")
    fun delete(@RequestParam category: String) = categoryRepository.delete(category)
}