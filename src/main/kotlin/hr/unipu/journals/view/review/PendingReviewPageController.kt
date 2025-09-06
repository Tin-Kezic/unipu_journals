package hr.unipu.journals.view.review

import hr.unipu.journals.feature.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.manuscript.ManuscriptRepository
import hr.unipu.journals.feature.publication.PublicationRepository
import hr.unipu.journals.view.home.ManuscriptDTO
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.time.format.DateTimeFormatter

@Controller
class PendingReviewPageController(
    private val publicationRepository: PublicationRepository,
    private val manuscriptRepository: ManuscriptRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
) {
    @GetMapping("/review")
    fun all(model: Model): String {
        model["publicationsSidebar"] = publicationRepository.allUnderReview()
        model["manuscripts"] = manuscriptRepository.allUnderReview().map { manuscript ->
            ManuscriptDTO(
                id = manuscript.id,
                title = manuscript.title,
                authors = accountRoleOnManuscriptRepository.authors(manuscript.id),
                publicationDate = manuscript.publicationDate?.format(DateTimeFormatter.ofPattern("dd MMM YYYY")) ?: "no publication date",
                description = manuscript.description
            )
        }
        return "review/pending-review-page"
    }
    @GetMapping("review/from-publication/{publicationId}")
    fun underPublication(@PathVariable publicationId: Int, model: Model): String {
        model["publicationsSidebar"] = publicationRepository.allPublished()
        model["manuscripts"] = manuscriptRepository.allUnderReviewByPublication(publicationId).map { manuscript ->
            ManuscriptDTO(
                id = manuscript.id,
                title = manuscript.title,
                authors = accountRoleOnManuscriptRepository.authors(manuscript.id),
                publicationDate = manuscript.publicationDate?.format(DateTimeFormatter.ofPattern("dd MMM YYYY")) ?: "no publication date",
                description = manuscript.description
            )
        }
        return "review/pending-review-page"
    }
}