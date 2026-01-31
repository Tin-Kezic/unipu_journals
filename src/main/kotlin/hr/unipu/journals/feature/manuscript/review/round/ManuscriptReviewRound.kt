package hr.unipu.journals.feature.manuscript.review.round

import hr.unipu.journals.feature.manuscript.review.Recommendation
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("manuscript_review_round")
data class ManuscriptReviewRound(
    @Id val id: Int,
    val manuscriptId: Int,
    val round: Int,
    val editorRecommendation: Recommendation?,
    val editorComment: String?
) {
    val isComplete: Boolean
        get() = editorRecommendation == null
}