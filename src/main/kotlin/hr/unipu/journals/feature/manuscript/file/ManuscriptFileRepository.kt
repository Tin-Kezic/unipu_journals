package hr.unipu.journals.feature.manuscript.file

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface ManuscriptFileRepository: Repository<ManuscriptFile, Int> {
    @Query("SELECT * FROM manuscript_file WHERE id = :id")
    fun byId(@Param("id") id: Int): ManuscriptFile?

    @Query("SELECT * FROM manuscript_file WHERE manuscript_id = :manuscript_id")
    fun allFilesByManuscriptId(@Param("manuscript_id") manuscriptId: Int): List<ManuscriptFile>

    @Modifying
    @Query("INSERT INTO manuscript_file (name, path, manuscript_id) VALUES (:name, :path, :manuscript_id)")
    fun insert(
        @Param("name") name: String,
        @Param("path") path: String,
        @Param("manuscript_id") manuscriptId: Int
    ): Int
}