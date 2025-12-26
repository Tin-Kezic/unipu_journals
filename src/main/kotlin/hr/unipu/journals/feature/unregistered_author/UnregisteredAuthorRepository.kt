package hr.unipu.journals.feature.unregistered_author

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface UnregisteredAuthorRepository: Repository<UnregisteredAuthor, Int> {
    @Query("SELECT * FROM unregistered_author WHERE manuscript_id = :manuscript_id")
    fun authors(@Param("manuscript_id") manuscriptId: Int): List<UnregisteredAuthor>

    @Query("SELECT * FROM unregistered_author WHERE email = :email")
    fun byEmail(@Param("email") email: String): UnregisteredAuthor?

    @Query("INSERT INTO unregistered_authors (full_name, email, country, affiliation, manuscript_id) VALUES (:full_name, :email, :country, :affiliation, :manuscript_id)")
    fun insert(
        @Param("full_name") fullName: String,
        @Param("email") email: String,
        @Param("country") country: String,
        @Param("affiliation") affiliation: String,
        @Param("manuscript_id") manuscriptId: Int,
    )
}