package hr.unipu.journals.feature.publication

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

private const val PUBLICATION = "publication"
interface PublicationRepository: Repository<Publication, Int> {

    // view
    @Query("SELECT * FROM $PUBLICATION")
    fun all(): List<Publication>

    @Query("SELECT * FROM $PUBLICATION WHERE id = :id")
    fun findById(@Param("id") id: Int): Publication

    // REST
    @Modifying
    @Query("INSERT INTO $PUBLICATION (title) VALUES (:title)")
    fun insert(@Param("title") title: String)

    @Modifying
    @Query("UPDATE $PUBLICATION SET title = :title WHERE id = :id")
    fun update(@Param("id") id: Int, @Param("title") title: String)

    @Query("SELECT EXISTS (SELECT 1 FROM $PUBLICATION WHERE id = :id)")
    fun existsById(@Param("id") id: Int): Boolean

    @Modifying
    @Query("UPDATE $PUBLICATION SET is_hidden = TRUE where id = :id")
    fun hide(@Param("id") id: Int)

    @Modifying
    @Query("DELETE FROM $PUBLICATION WHERE id = :id")
    fun delete(@Param("id") id: Int)
}