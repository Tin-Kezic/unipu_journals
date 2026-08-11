package hr.unipu.journals.view.review

import hr.unipu.journals.feature.manuscript.core.ManuscriptRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptService
import hr.unipu.journals.feature.manuscript.review.core.ManuscriptReviewRepository
import hr.unipu.journals.feature.manuscript.review.round.ManuscriptReviewRoundRepository
import hr.unipu.journals.security.AuthorizationService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Controller
class ReviewHistoryPageController(
    private val authorizationService: AuthorizationService,
    private val manuscriptRepository: ManuscriptRepository,
    private val manuscriptService: ManuscriptService,
    private val manuscriptReviewRepository: ManuscriptReviewRepository,
    private val manuscriptReviewRoundRepository: ManuscriptReviewRoundRepository,
) {
    @GetMapping("/manuscripts/{manuscriptId}/review-history")
    fun page(@PathVariable manuscriptId: Int, @RequestParam id: Int?, model: Model): String {
        val manuscript = manuscriptRepository.byId(manuscriptId) ?: throw IllegalArgumentException("failed to find manuscript $manuscriptId")
        model["manuscript"] = manuscriptService.toManuscriptDto(manuscript)
        val latestRound = manuscriptReviewRoundRepository.latest(manuscript.id)
        val reviews = manuscriptReviewRepository.all(manuscriptId = manuscript.id)
        model["awaitingResponse"] = manuscript.correspondingAuthorEmail == authorizationService.account?.email && reviews.last().authorResponse == null
        model["ongoingRound"] = latestRound?.isComplete?.not()
        return "review/review-history"
    }
}