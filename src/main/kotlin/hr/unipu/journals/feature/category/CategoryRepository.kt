package hr.unipu.journals.feature.category

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface CategoryRepository: Repository<Category, Int> {
    @Query("SELECT name FROM category ORDER BY name")
    fun all(): List<String>

    @Modifying
    @Query("INSERT INTO category (name) VALUES (:name)")
    fun insert(@Param("name") title: String): Int

    @Modifying
    @Query("DELETE FROM category WHERE name = :name")
    fun delete(@Param("name") category: String) : Int
}
