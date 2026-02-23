package hr.unipu.journals.feature.manuscript.review.file

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface ManuscriptReviewFileRepository: Repository<ManuscriptReviewFile, Int> {
    @Query("SELECT * FROM manuscript_review_file WHERE id = :id")
    fun byId(@Param("id") id: Int): ManuscriptReviewFile?

    @Query("SELECT * FROM manuscript_review_file WHERE review_id = :review_id")
    fun all(@Param("review_id") reviewId: Int): List<ManuscriptReviewFile>

    @Modifying
    @Query("INSERT INTO manuscript_review_file (name, path, review_id, file_role) VALUES (:name, :path, :review_id, :file_role::review_file_role)")
    fun insert(
        @Param("name") name: String,
        @Param("path") path: String,
        @Param("review_id") reviewId: Int,
        @Param("file_role") fileRole: ManuscriptReviewFileRole
    ): Int
}