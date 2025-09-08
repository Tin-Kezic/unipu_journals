package hr.unipu.journals.feature.category

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository

private const val CATEGORY = "category"
private const val ID = "id"
private const val NAME = "name"
interface CategoryRepository: Repository<Category, Int> {
    @Query("SELECT * FROM $CATEGORY")
    fun all(): List<Category>
}
