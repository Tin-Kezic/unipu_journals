package hr.unipu.journals.feature.manuscript.review

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("manuscript_review")
data class ManuscriptReview(
    @Id val id: Int,
    val manuscriptId: Int,
    val reviewerId: Int,
    val manuscriptReviewRoundId: Int,
    val novelty: OneToFive?,
    val significance: OneToFive?,
    val technicalQuality: OneToFive?,
    val clarity: OneToFive?,
    val methodology: OneToFive?,
    val relevanceToThePublication: OneToFive?,
    val languageQuality: OneToFive?,
    val overallMark: OneToFive?,
    val sufficientBackground: ReviewQuestion?,
    val appropriateResearchDesign: ReviewQuestion?,
    val adequatelyDescribed: ReviewQuestion?,
    val clearlyPresented: ReviewQuestion?,
    val supportedConclusions: ReviewQuestion?,
    val conflict: Boolean?,
    val plagiarism: Boolean?,
    val llm: Boolean?,
    val selfCitation: Boolean?,
    val appropriateReferences: Boolean?,
    val ethicalConcerns: Boolean?,
    val reviewerComment: String?,
    val reviewerCommentFileUrl: String?,
    val authorResponse: String?,
    val authorResponseFileUrl: String?,
    val recommendation: Recommendation?,
    val reviewDate: LocalDateTime?,
    val authorResponseDate: LocalDateTime?,
)