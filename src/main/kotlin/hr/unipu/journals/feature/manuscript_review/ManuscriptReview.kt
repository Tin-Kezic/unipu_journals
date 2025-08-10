package hr.unipu.journals.feature.manuscript_review

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("manuscript_review")
data class ManuscriptReview(
    @Id val id: Int,
    val manuscriptId: Int,
    val reviewerId: Int,
    val round: Int,
    val reviewerComment: String?,
    val reviewerCommentFileUrl: String?,
    val authorResponse: String?,
    val authorResponseFileUrl: String?,
    val reviewDate: LocalDateTime?,
    val authorResponseDate: LocalDateTime?,
)