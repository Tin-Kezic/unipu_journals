package hr.unipu.journals.feature.manuscript.file

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface ManuscriptFileRepository: Repository<ManuscriptFile, Int> {
    @Query("SELECT path FROM file WHERE manuscript_id = :manuscript_id")
    fun allPathsByManuscriptId(@Param("manuscript_id") manuscriptId: Int): String

    @Query("INSERT INTO file (path, manuscript_id) VALUES (:path, :manuscript_id)")
    fun insert(@Param("path") path: String, @Param("manuscript_id") manuscriptId: Int): Int
}