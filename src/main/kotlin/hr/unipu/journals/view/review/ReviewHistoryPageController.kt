package hr.unipu.journals.view.review

import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptRepository
import hr.unipu.journals.feature.manuscript.review.ManuscriptReviewRepository
import hr.unipu.journals.feature.manuscript.review.OneToFive
import hr.unipu.journals.feature.manuscript.review.Recommendation
import hr.unipu.journals.feature.manuscript.review.ReviewQuestion
import hr.unipu.journals.feature.manuscript.review.ReviewerWithRounds
import hr.unipu.journals.view.ResourceNotFoundException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.time.format.DateTimeFormatter

@Controller
class ReviewHistoryPageController(
    private val manuscriptRepository: ManuscriptRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
    private val manuscriptReviewRepository: ManuscriptReviewRepository
) {
    @GetMapping("/manuscript/{manuscriptId}/review-history/reviewer/{reviewerId}/round/{round}")
    fun page(
        @PathVariable manuscriptId: Int,
        @PathVariable reviewerId: Int,
        @PathVariable round: Int,
        model: Model
    ): String {
        model["isUnderReviewAndUserIsAffiliated"] = 0
        val manuscript = manuscriptRepository.byId(manuscriptId) ?: throw ResourceNotFoundException("failed to find manuscript $manuscriptId")
        model["id"] = manuscriptId
        model["title"] = manuscript.title
        model["submissionDate"] = manuscript.submissionDate.format(DateTimeFormatter.ofPattern("dd.MM.YYYY"))
        model["publicationDate"] = manuscript.publicationDate?.format(DateTimeFormatter.ofPattern("dd.MM.YYYY")) ?: "no publication date"
        model["authors"] = accountRoleOnManuscriptRepository.authors(manuscript.id)
        model["abstract"] = manuscript.description
        model["fileUrl"] = manuscript.fileUrl
        model["views"] = manuscript.views
        model["downloads"] = manuscript.downloads
        model["reviewerWithRounds"] = manuscriptReviewRepository.reviewersAndRounds(manuscriptId)
            .groupBy { it.reviewerId }
            .map { (reviewer, rounds) -> ReviewerWithRounds(reviewer, rounds.map { it.round }) }
        val manuscriptReview = manuscriptReviewRepository.review(manuscriptId, reviewerId, round)
        model["round"] = manuscriptReview.round

        when(manuscriptReview.novelty) {
            OneToFive.ONE -> model["novelty1"] = true
            OneToFive.TWO -> model["novelty2"] = true
            OneToFive.THREE -> model["novelty3"] = true
            OneToFive.FOUR -> model["novelty4"] = true
            OneToFive.FIVE -> model["novelty5"] = true
        }
        when(manuscriptReview.significance) {
            OneToFive.ONE -> model["significance1"] = true
            OneToFive.TWO -> model["significance2"] = true
            OneToFive.THREE -> model["significance3"] = true
            OneToFive.FOUR -> model["significance4"] = true
            OneToFive.FIVE -> model["significance5"] = true
        }
        when(manuscriptReview.technicalQuality) {
            OneToFive.ONE -> model["technicalQuality1"] = true
            OneToFive.TWO -> model["technicalQuality2"] = true
            OneToFive.THREE -> model["technicalQuality3"] = true
            OneToFive.FOUR -> model["technicalQuality4"] = true
            OneToFive.FIVE -> model["technicalQuality5"] = true
        }
        when(manuscriptReview.clarity) {
            OneToFive.ONE -> model["clarity1"] = true
            OneToFive.TWO -> model["clarity2"] = true
            OneToFive.THREE -> model["clarity3"] = true
            OneToFive.FOUR -> model["clarity4"] = true
            OneToFive.FIVE -> model["clarity5"] = true
        }
        when(manuscriptReview.methodology) {
            OneToFive.ONE -> model["methodology1"] = true
            OneToFive.TWO -> model["methodology2"] = true
            OneToFive.THREE -> model["methodology3"] = true
            OneToFive.FOUR -> model["methodology4"] = true
            OneToFive.FIVE -> model["methodology5"] = true
        }
        when(manuscriptReview.relevanceToThePublication) {
            OneToFive.ONE -> model["relevanceToThePublication1"] = true
            OneToFive.TWO -> model["relevanceToThePublication2"] = true
            OneToFive.THREE -> model["relevanceToThePublication3"] = true
            OneToFive.FOUR -> model["relevanceToThePublication4"] = true
            OneToFive.FIVE -> model["relevanceToThePublication5"] = true
        }
        when(manuscriptReview.languageQuality) {
            OneToFive.ONE -> model["languageQuality1"] = true
            OneToFive.TWO -> model["languageQuality2"] = true
            OneToFive.THREE -> model["languageQuality3"] = true
            OneToFive.FOUR -> model["languageQuality4"] = true
            OneToFive.FIVE -> model["languageQuality5"] = true
        }
        when(manuscriptReview.overallMark) {
            OneToFive.ONE -> model["overallMark1"] = true
            OneToFive.TWO -> model["overallMark2"] = true
            OneToFive.THREE -> model["overallMark3"] = true
            OneToFive.FOUR -> model["overallMark4"] = true
            OneToFive.FIVE -> model["overallMark5"] = true
        }
        when(manuscriptReview.sufficientBackground) {
            ReviewQuestion.YES -> model["sufficientBackground1"] = true
            ReviewQuestion.CAN_BE_IMPROVED -> model["sufficientBackground2"] = true
            ReviewQuestion.MUST_BE_IMPROVED -> model["sufficientBackground3"] = true
        }
        when(manuscriptReview.appropriateResearchDesign) {
            ReviewQuestion.YES -> model["appropriateResearchDesign1"] = true
            ReviewQuestion.CAN_BE_IMPROVED -> model["appropriateResearchDesign2"] = true
            ReviewQuestion.MUST_BE_IMPROVED -> model["appropriateResearchDesign3"] = true
        }
        when(manuscriptReview.adequatelyDescribed) {
            ReviewQuestion.YES -> model["adequatelyDescribed1"] = true
            ReviewQuestion.CAN_BE_IMPROVED -> model["adequatelyDescribed2"] = true
            ReviewQuestion.MUST_BE_IMPROVED -> model["adequatelyDescribed3"] = true
        }
        when(manuscriptReview.clearlyPresented) {
            ReviewQuestion.YES -> model["clearlyPresented1"] = true
            ReviewQuestion.CAN_BE_IMPROVED -> model["clearlyPresented2"] = true
            ReviewQuestion.MUST_BE_IMPROVED -> model["clearlyPresented3"] = true
        }
        when(manuscriptReview.supportedConclusions) {
            ReviewQuestion.YES -> model["supportedConclusions1"] = true
            ReviewQuestion.CAN_BE_IMPROVED -> model["supportedConclusions2"] = true
            ReviewQuestion.MUST_BE_IMPROVED -> model["supportedConclusions3"] = true
        }
        model["conflict"] = manuscriptReview.conflict
        model["plagiarism"] = manuscriptReview.plagiarism
        model["llm"] = manuscriptReview.llm
        model["selfCitation"] = manuscriptReview.selfCitation
        model["appropriateReferences"] = manuscriptReview.appropriateReferences
        model["ethicalConcerns"] = manuscriptReview.ethicalConcerns
        model["reviewerComment"] = manuscriptReview.reviewerComment
        model["reviewerCommentFileUrl"] = manuscriptReview.reviewerCommentFileUrl
        model["authorResponse"] = manuscriptReview.authorResponse
        model["authorResponseFileUrl"] = manuscriptReview.authorResponseFileUrl
        model["reviewDate"] = manuscriptReview.reviewDate
        model["authorResponseDate"] = manuscriptReview.authorResponseDate

        when(manuscriptReview.recommendation) {
            Recommendation.ACCEPT -> model["accept"] = true
            Recommendation.MAJOR -> model["major"] = true
            Recommendation.MINOR -> model["minor"] = true
            Recommendation.REJECT -> model["reject"] = true
        }
        return "review/review-history"
    }
}
