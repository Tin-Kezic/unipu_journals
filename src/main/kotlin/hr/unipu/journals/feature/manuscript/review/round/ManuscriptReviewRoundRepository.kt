package hr.unipu.journals.feature.manuscript.review.round

import hr.unipu.journals.feature.manuscript.review.Recommendation
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface ManuscriptReviewRoundRepository: Repository<ManuscriptReviewRound, Int> {

    @Query("SELECT * FROM manuscript_review_round WHERE manuscript_id = :manuscript_id ORDER BY round DESC LIMIT 1")
    fun by(@Param("manuscript_id") manuscriptId: Int): ManuscriptReviewRound?

    @Transactional
    @Query("""
        INSERT INTO manuscript_review_round (manuscript_id, round)
        SELECT
            :manuscript_id,
            COALESCE(MAX(round), 0) + 1
        FROM (
            SELECT round
            FROM manuscript_review_round
            WHERE manuscript_id = :manuscript_id
            FOR UPDATE
        ) locked_rows
        RETURNING *
    """)
    fun startRound(@Param("manuscript_id") manuscriptId: Int): ManuscriptReviewRound?

    @Modifying
    @Transactional
    @Query("""
        UPDATE manuscript_review_round
        SET
            editor_recommendation = :recommendation::review_recommendation,
            editor_comment = :comment
        WHERE manuscript_id = :manuscript_id
        AND round = (
            SELECT MAX(round)
            FROM (
                SELECT round
                FROM manuscript_review_round
                WHERE manuscript_id = :manuscript_id
                FOR UPDATE
            ) locked_rows
        )
    """)
    fun endRound(
        @Param("manuscript_id") manuscriptId: Int,
        @Param("recommendation") recommendation: Recommendation,
        @Param("comment") comment: String
    ): Int
}