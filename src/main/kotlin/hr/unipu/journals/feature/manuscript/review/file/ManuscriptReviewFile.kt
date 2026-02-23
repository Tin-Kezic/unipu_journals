package hr.unipu.journals.feature.manuscript.review.file

import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.annotation.Id

@Table("manuscript_review_file")
data class ManuscriptReviewFile(
    @Id val id: Int,
    val name: String,
    val path: String,
    val reviewId: Int,
    val fileRole: ManuscriptReviewFileRole,
)