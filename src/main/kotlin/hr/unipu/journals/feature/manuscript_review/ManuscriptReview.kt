package hr.unipu.journals.feature.manuscript_review

import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("manuscript_review")
data class ManuscriptReview(
    val id: Int,
    val manuscriptId: Int,
    val reviewerId: Int,
    val round: Int,
    val novelty: OneToFive,
    val significance: OneToFive,
    val technicalQuality: OneToFive,
    val clarity: OneToFive,
    val methodology: OneToFive,
    val relevanceToThePublication: OneToFive,
    val languageQuality: OneToFive,
    val overallMark: OneToFive,
    val sufficientBackground: ReviewQuestion,
    val appropriateResearchDesign: ReviewQuestion,
    val adequatelyDescribed: ReviewQuestion,
    val clearlyPresented: ReviewQuestion,
    val supportedConclusions: ReviewQuestion,
    val conflict: Boolean,
    val plagiarism: Boolean,
    val llm: Boolean,
    val selfCitation: Boolean,
    val appropriateReferences: Boolean,
    val ethicalConcerns: Boolean,
    val reviewerComment: String,
    val reviewerCommentFileUrl: String,
    val authorResponse: String,
    val authorResponseFileUrl: String,
    val recommendation: Recommendation,
    val reviewDate: LocalDateTime,
    val authorResponseDate: LocalDateTime,
)