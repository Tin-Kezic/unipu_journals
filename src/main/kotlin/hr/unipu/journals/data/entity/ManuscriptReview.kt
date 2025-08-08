package hr.unipu.journals.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("manuscript_review")
data class ManuscriptReview(
    @Id val id: Int? = null,
    val manuscriptId: Int,
    val round: Int,
    val reviewer: Int,
    val reviewerComment: String?,
    val reviewerCommentFileUrl: String?,
    val authorResponse: String?,
    val authorResponseFileUrl: String?,
    val reviewDate: LocalDateTime?,
    val authorResponseDate: LocalDateTime?,
)