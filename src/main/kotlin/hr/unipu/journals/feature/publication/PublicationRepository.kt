package hr.unipu.journals.feature.publication

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface PublicationRepository: Repository<Publication, Int> {

    @Modifying
    @Query("INSERT INTO publication (title) VALUES (:title)")
    fun insert(@Param("title") title: String)

    @Modifying
    @Query("UPDATE publication SET title = :title WHERE id = :id")
    fun update(@Param("id") id: Int, @Param("title") title: String)

    @Query("SELECT * FROM publication")
    fun all(): List<Publication>

    @Query("SELECT * FROM publication WHERE id = :id")
    fun findById(@Param("id") id: Int): Publication

    @Query("SELECT EXISTS (SELECT 1 FROM publication WHERE id = :id)")
    fun existsById(@Param("id") id: Int): Boolean

    @Modifying
    @Query("UPDATE publication SET is_hidden = TRUE where id = :id")
    fun hide(@Param("id") id: Int)

    @Modifying
    @Query("DELETE FROM publication WHERE id = :id")
    fun delete(@Param("id") id: Int)
}