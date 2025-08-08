package hr.unipu.journals.data.repository

import hr.unipu.journals.data.entity.Publication
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface PublicationRepository: CrudRepository<Publication, Int> {

    @Modifying
    @Query("INSERT INTO publication (title) VALUES (:title)")
    fun insertPublication(@Param("title") title: String)

    @Modifying
    @Query("UPDATE publication SET title = :title WHERE id = :id")
    fun updatePublication(@Param("id") id: Int, @Param("title") title: String)

    @Modifying
    @Query("UPDATE publication SET is_hidden = TRUE where id = :id")
    fun hidePublication(@Param("id") id: Int)
}