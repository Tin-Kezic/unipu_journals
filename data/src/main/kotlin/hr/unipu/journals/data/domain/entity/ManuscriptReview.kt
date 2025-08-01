package hr.unipu.journals.data.domain.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("manuscript_review")
data class ManuscriptReview(
    @Id val id: Int,
    val manuscriptId: Int,
    val reviewer: Int,
    val reviewerCommentFileUrl: String,
    val authorResponseFileUrl: String,
    val reviewDate: LocalDateTime,
    val authorResponseDate: LocalDateTime,
)
