package hr.unipu.journals.feature.manuscript.file

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface ManuscriptFileRepository: Repository<ManuscriptFile, Int> {
    @Query("SELECT * FROM file WHERE manuscript_id = :manuscript_id")
    fun allFilesByManuscriptId(@Param("manuscript_id") manuscriptId: Int): List<ManuscriptFile>

    @Query("INSERT INTO file (name, path, manuscript_id) VALUES (:name, :path, :manuscript_id)")
    fun insert(
        @Param("name") name: String,
        @Param("path") path: String,
        @Param("manuscript_id") manuscriptId: Int
    ): Int
}