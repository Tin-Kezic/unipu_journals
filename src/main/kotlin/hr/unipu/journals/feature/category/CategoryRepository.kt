package hr.unipu.journals.feature.category

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

private const val CATEGORY = "category"
private const val ID = "id"
private const val NAME = "name"
interface CategoryRepository: Repository<Category, Int> {
    @Query("SELECT $NAME FROM $CATEGORY")
    fun all(): List<String>

    @Modifying
    @Query("INSERT INTO $CATEGORY ($NAME) VALUES (:$NAME)")
    fun insert(@Param(NAME) title: String)

    @Modifying
    @Query("DELETE FROM $CATEGORY WHERE $NAME = :$NAME")
    fun delete(@Param(NAME) category: String)
}
