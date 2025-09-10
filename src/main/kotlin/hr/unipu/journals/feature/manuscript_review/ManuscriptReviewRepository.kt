package hr.unipu.journals.feature.manuscript_review

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

private const val MANUSCRIPT_REVIEW = "manuscript_review"
private const val ID = "id"
private const val MANUSCRIPT_ID = "manuscript_id"
private const val REVIEWER_ID = "reviewer_id"
private const val ROUND = "round"
private const val REVIEWER_COMMENT = "reviewer_comment"
private const val NOVELTY = "novelty"
private const val SIGNIFICANCE = "significance"
private const val TECHNICAL_QUALITY = "technical_quality"
private const val CLARITY = "clarity"
private const val METHODOLOGY = "methodology"
private const val RELEVANCE_TO_THE_PUBLICATION = "relevance_to_the_publication"
private const val LANGUAGE_QUALITY = "language_quality"
private const val OVERALL_MARK = "overall_mark"
private const val SUFFICIENT_BACKGROUND = "sufficient_background"
private const val APPROPRIATE_RESEARCH_DESIGN = "appropriate_research_design"
private const val ADEQUATELY_DESCRIBED = "adequately_described"
private const val CLEARLY_PRESENTED = "clearly_presented"
private const val SUPPORTED_CONCLUSIONS = "supported_conclusions"
private const val CONFLICT = "conflict"
private const val PLAGIARISM = "plagiarism"
private const val LLM = "llm"
private const val SELF_CITATION = "self_citation"
private const val APPROPRIATE_REFERENCES = "appropriate_references"
private const val ETHICAL_CONCERNS = "ethical_concerns"
private const val REVIEWER_COMMENT_FILE_URL = "reviewer_comment_file_url"
private const val AUTHOR_RESPONSE = "author_response"
private const val AUTHOR_RESPONSE_FILE_URL = "author_response_file_url"
private const val RECOMMENDATION = "recommendation"
private const val REVIEW_DATE = "review_date"
private const val AUTHOR_RESPONSE_DATE = "author_response_date"
interface ManuscriptReviewRepository: Repository<ManuscriptReview, Int> {
    @Query("SELECT * FROM $MANUSCRIPT_REVIEW WHERE $MANUSCRIPT_ID = :$MANUSCRIPT_ID AND $ROUND = :$ROUND AND $REVIEWER_ID = :$REVIEWER_ID")
    fun review(
        @Param(MANUSCRIPT_ID) manuscriptId: Int,
        @Param(ROUND) round: Int,
        @Param(REVIEWER_ID) reviewerId: Int
    ): ManuscriptReview

    @Query("SELECT $REVIEWER_ID, $ROUND FROM $MANUSCRIPT_REVIEW WHERE $MANUSCRIPT_ID = :$MANUSCRIPT_ID")
    fun reviewersAndRounds(@Param(MANUSCRIPT_ID) manuscriptId: Int): List<ReviewerAndRound>
}