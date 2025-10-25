package hr.unipu.journals.repository_tests

import hr.unipu.journals.feature.manuscript.review.ManuscriptReview
import hr.unipu.journals.feature.manuscript.review.ManuscriptReviewRepository
import hr.unipu.journals.feature.manuscript.review.OneToFive
import hr.unipu.journals.feature.manuscript.review.Recommendation
import hr.unipu.journals.feature.manuscript.review.ReviewQuestion
import hr.unipu.journals.feature.manuscript.review.ReviewerAndRound
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

@DataJdbcTest
@Suppress("BooleanLiteralArgument")
class ManuscriptReviewRepositoryTests {
    @Autowired private lateinit var manuscriptReviewRepository: ManuscriptReviewRepository

    @Test fun `retrieve review by manuscript id, reviewer id and round`() {
        assertEquals(
            ManuscriptReview(1, 1, 1, 1, OneToFive.FOUR, OneToFive.FOUR, OneToFive.FIVE, OneToFive.FOUR, OneToFive.FIVE, OneToFive.FOUR, OneToFive.FOUR, OneToFive.FOUR, ReviewQuestion.YES, ReviewQuestion.YES, ReviewQuestion.YES, ReviewQuestion.YES, ReviewQuestion.YES, false, true, false, true, true, false, "Excellent study with clear methodology.", "http://example.com/rev1.pdf", "Thank you!", "http://example.com/res1.pdf", Recommendation.ACCEPT, LocalDateTime.of(2020, 2, 28, 13, 28, 0), LocalDateTime.of(2022, 2, 28, 13, 28, 0)),
            manuscriptReviewRepository.review(
                manuscriptId = 1,
                reviewerId = 1,
                round = 1
            )
        )
    }
    @Test fun `retrieve reviewers and rounds`() {
        assertEquals(
            listOf(
                ReviewerAndRound(1, 1),
                ReviewerAndRound(2, 1),
                ReviewerAndRound(2, 2),
                ReviewerAndRound(2, 3),
                ReviewerAndRound(3, 1),
                ReviewerAndRound(3, 2),
            ),
            manuscriptReviewRepository.reviewersAndRounds(1)
        )
    }
}