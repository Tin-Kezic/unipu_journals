package hr.unipu.journals.view.home

import hr.unipu.journals.feature.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.manuscript.ManuscriptRepository
import hr.unipu.journals.feature.section.SectionRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.time.format.DateTimeFormatter

@Controller
@RequestMapping("/publication")
class ManuscriptPageController(
    private val sectionRepository: SectionRepository,
    private val manuscriptRepository: ManuscriptRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository
) {
    @GetMapping("/{publicationId}/section/{sectionId}")
    fun page(
        @PathVariable publicationId: Int,
        @PathVariable sectionId: Int,
        model: Model
    ): String {
        model["sectionsSidebar"] = sectionRepository.allPublishedByPublicationId(publicationId)
        model["description"] = sectionRepository.description(sectionId)
        model["manuscripts"] = manuscriptRepository.allPublishedBySectionId(sectionId).map { manuscript ->
            ManuscriptDTO(
                id = manuscript.id,
                title = manuscript.title,
                authors = accountRoleOnManuscriptRepository.authors(manuscript.id),
                publicationDate = manuscript.publicationDate?.format(DateTimeFormatter.ofPattern("dd MMM YYYY")) ?: "no publication date",
                description = manuscript.description
            )
        }
        model["title"] = sectionRepository.title(sectionId)
        return "home/manuscript-page"
    }
}