package hr.unipu.journals.view.archive

import hr.unipu.journals.feature.manuscript.core.ManuscriptState
import hr.unipu.journals.feature.publication.core.PublicationRepository
import hr.unipu.journals.feature.publication.core.PublicationType
import hr.unipu.journals.feature.section.core.SectionRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class ArchiveSectionPageController(
    private val publicationRepository: PublicationRepository,
    private val sectionRepository: SectionRepository
) {
    @GetMapping("archive/publication/{publicationId}")
    fun page(@PathVariable publicationId: Int, model: Model): String {
        model["publicationsSidebar"] = publicationRepository.all(PublicationType.CONTAINS_ARCHIVED_MANUSCRIPTS)
        model["currentPublication"] = publicationRepository.title(publicationId)
        model["sections"] = sectionRepository.allByPublicationId(publicationId, ManuscriptState.ARCHIVED).map { section ->
            ArchiveContainerDTO(
                id = section.id,
                title = section.title,
            )
        }
        return "archive/archive-section-page"
    }
}