package hr.unipu.journals.feature.manuscript.review

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface ManuscriptReviewRepository: Repository<ManuscriptReview, Int> {
    @Query("""
        SELECT manuscript_review.* FROM manuscript_review
        JOIN manuscript_review_round ON manuscript_review.manuscript_review_round_id = manuscript_review_round.id
        WHERE (manuscript_review_round.manuscript_id = :manuscript_id OR :manuscript_id IS NULL)
        AND (manuscript_review_round.round = :round OR :round IS NULL)
        AND (manuscript_review.reviewer_id = :reviewer_id OR :reviewer_id IS NULL)
    """)
    fun all(
        @Param("manuscript_id") manuscriptId: Int? = null,
        @Param("reviewer_id") reviewerId: Int? = null,
        @Param("round") round: Int? = null
    ): List<ManuscriptReview>

    @Query("SELECT * FROM manuscript_review WHERE id = :id")
    fun byId(@Param("id") id: Int): ManuscriptReview?

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM manuscript_review
            WHERE manuscript_review_round_id = :manuscript_review_round_id
            AND reviewer_id = :reviewer_id
            AND overall_mark IS NOT NULL)
    """)
    fun isComplete(
        @Param("manuscript_review_round_id") manuscriptReviewRoundId: Int,
        @Param("reviewer_id") reviewerId: Int,
    ): Boolean

    @Modifying
    @Query("INSERT INTO manuscript_review (reviewer_id, manuscript_review_round_id) VALUES (:reviewer_id, :manuscript_review_round_id)")
    fun insert(
        @Param("reviewer_id") reviewerId: Int,
        @Param("manuscript_review_round_id") manuscriptReviewRoundId: Int,
    ): Int

    @Query("""
        UPDATE manuscript_review SET
            novelty = :#{#dto.novelty?.name}::one_to_five,
            significance = :#{#dto.significance?.name}::one_to_five,
            technical_quality = :#{#dto.technicalQuality?.name}::one_to_five,
            clarity = :#{#dto.clarity?.name}::one_to_five,
            methodology = :#{#dto.methodology?.name}::one_to_five,
            relevance_to_the_publication = :#{#dto.relevanceToThePublication?.name}::one_to_five,
            language_quality = :#{#dto.languageQuality?.name}::one_to_five,
            overall_mark = :#{#dto.overallMark?.name}::one_to_five,
            sufficient_background = :#{#dto.sufficientBackground?.name}::review_question,
            appropriate_research_design = :#{#dto.appropriateResearchDesign?.name}::review_question,
            adequately_described = :#{#dto.adequatelyDescribed?.name}::review_question,
            clearly_presented = :#{#dto.clearlyPresented?.name}::review_question,
            supported_conclusions = :#{#dto.supportedConclusions?.name}::review_question,
            conflict = :#{#dto.conflictOfInterest},
            plagiarism = :#{#dto.plagiarism},
            llm = :#{#dto.llmUsed},
            self_citation = :#{#dto.selfCitation},
            appropriate_references = :#{#dto.appropriateReferences},
            ethical_concerns = :#{#dto.ethicalConcerns},
            reviewer_comment = :#{#dto.reviewerComment},
            recommendation = :#{#dto.recommendation?.name}::review_recommendation,
            review_date = CURRENT_TIMESTAMP
        WHERE manuscript_review_round_id = :manuscript_review_round_id
        AND reviewer_id = :reviewer_id
        RETURNING *
    """)
    fun review(
        @Param("manuscript_review_round_id") manuscriptReviewRoundId: Int,
        @Param("reviewer_id") reviewerId: Int,
        @Param("dto") manuscriptReviewDTO: ManuscriptReviewDTO
    ): ManuscriptReview

    @Modifying
    @Query("UPDATE manuscript_review SET author_response = :response, author_response_date = CURRENT_TIMESTAMP")
    fun authorRespond(
        @Param("manuscript_review_round_id") manuscriptReviewRoundId: Int,
        @Param("response") response: String
    ): Int
}