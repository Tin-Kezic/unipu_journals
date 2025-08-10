package hr.unipu.journals.feature.publication

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

private const val PUBLICATION = "publication"
private const val ID = "id"
private const val TITLE = "title"
private const val IS_HIDDEN = "is_hidden"

interface PublicationRepository: Repository<Publication, Int> {
    // view
    @Query("SELECT * FROM $PUBLICATION")
    fun all(): List<Publication>

    // REST
    @Modifying
    @Query("INSERT INTO $PUBLICATION ($TITLE) VALUES (:$TITLE)")
    fun insert(@Param(TITLE) title: String)

    @Modifying
    @Query("UPDATE $PUBLICATION SET $TITLE = :$TITLE WHERE $ID = :$ID")
    fun updateTitle(@Param(ID) id: Int, @Param("title") title: String)

    @Query("SELECT EXISTS (SELECT 1 FROM $PUBLICATION WHERE $ID = :$ID)")
    fun existsById(@Param(ID) id: Int): Boolean

    @Modifying
    @Query("UPDATE $PUBLICATION SET $IS_HIDDEN = TRUE where $ID = :$ID")
    fun hide(@Param(ID) id: Int)

    @Modifying
    @Query("DELETE FROM $PUBLICATION WHERE $ID = :$ID")
    fun delete(@Param(ID) id: Int)
}