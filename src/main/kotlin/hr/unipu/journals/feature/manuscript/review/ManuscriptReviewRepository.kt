package hr.unipu.journals.feature.manuscript.review

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface ManuscriptReviewRepository: Repository<ManuscriptReview, Int> {
    @Query("SELECT * FROM manuscript_review WHERE manuscript_id = :manuscript_id AND round = :round AND reviewer_id = :reviewer_id")
    fun by(
        @Param("manuscript_id") manuscriptId: Int,
        @Param("reviewer_id") reviewerId: Int,
        @Param("round") round: Int
    ): ManuscriptReview

    @Query("SELECT reviewer_id, round FROM manuscript_review WHERE manuscript_id = :manuscript_id")
    fun reviewersAndRounds(@Param("manuscript_id") manuscriptId: Int): List<ReviewerAndRound>

    @Modifying
    @Query("INSERT INTO manuscript_review (reviewer_id, manuscript_review_round_id) VALUES (:reviewer_id, :manuscript_review_round_id)")
    fun insert(
        @Param("reviewer_id") reviewerId: Int,
        @Param("manuscript_review_round_id") manuscriptReviewRoundId: Int,
    ): Int

    @Modifying
    @Query("""
        UPDATE manuscript_review SET
            novelty = :#(#dto.novelty)::one_to_five,
            significance = :#(#dto.significance)::one_to_five,
            technical_quality = :#(#dto.technicalQuality)::one_to_five,
            clarity = :#(#dto.clarity)::one_to_five,
            methodology = :#(#dto.methodology)::one_to_five,
            relevance_to_the_publication = :#(#dto.relevanceToThePublication)::one_to_five,
            language_quality = :#(#dto.languageQuality)::one_to_five,
            overall_mark = :#(#dto.overallMark)::one_to_five,
            sufficient_background = :#(#dto.sufficientBackground)::review_question,
            appropriate_research_design = :#(#dto.appropriateResearchDesign)::review_question,
            adequately_described = :#(#dto.adequatelyDescribed)::review_question,
            clearly_presented = :#(#dto.clearlyPresented)::review_question,
            supported_conclusions = :#(#dto.supportedConclusions)::review_question,
            conflict = :#(#dto.conflict),
            plagiarism = :#(#dto.plagiarism),
            llm = :#(#dto.llm),
            self_citation = :#(#dto.selfCitation),
            appropriate_references = :#(#dto.appropriateReferences),
            ethical_concerns = :#(#dto.ethicalConcerns),
            reviewer_comment = :#(#dto.reviewerComment),
            reviewer_comment_file_url = :#(#dto.reviewerCommentFileUrl),
            recommendation = :#(#dto.recommendation)::review_recommendation,,
            review_date = CURRENT_TIMESTAMP
        WHERE manuscript_review_round_id = :manuscript_review_round_id
        AND reviewer_id = :reviewer_id
    """)
    fun review(
        @Param("manuscript_review_round_id") manuscriptReviewRoundId: Int,
        @Param("reviewer_id") reviewerId: Int,
        @Param("dto") manuscriptReviewDTO: ManuscriptReviewDTO
    ): Int

    @Modifying
    @Query("UPDATE manuscript_review SET author_response = :response, author_response_date = CURRENT_TIMESTAMP")
    fun authorRespond(
        @Param("manuscript_review_round_id") manuscriptReviewRoundId: Int,
        @Param("response") response: String
    ): Int
}