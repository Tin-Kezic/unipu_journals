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
}