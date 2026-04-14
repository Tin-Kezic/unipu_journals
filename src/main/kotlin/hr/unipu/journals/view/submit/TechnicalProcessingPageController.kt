package hr.unipu.journals.view.submit

import hr.unipu.journals.feature.manuscript.category.CategoryRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptService
import hr.unipu.journals.feature.manuscript.core.ManuscriptStateFilter
import hr.unipu.journals.feature.publication.core.PublicationRepository
import hr.unipu.journals.feature.section.core.SectionRepository
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_EIC_ON_MANUSCRIPT_OR_SUPERIOR
import hr.unipu.journals.view.ResourceNotFoundException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
@PreAuthorize(AUTHORIZATION_SERVICE_IS_EIC_ON_MANUSCRIPT_OR_SUPERIOR)
class TechnicalProcessingPageController(
    private val manuscriptRepository: ManuscriptRepository,
    private val manuscriptService: ManuscriptService,
    private val sectionRepository: SectionRepository,
    private val publicationRepository: PublicationRepository,
    private val categoryRepository: CategoryRepository
) {
    @GetMapping("/manuscripts/{manuscriptId}/technical-processing")
    fun page(@PathVariable manuscriptId: Int, model: Model): String {
        val manuscript = manuscriptRepository.byId(manuscriptId) ?: throw ResourceNotFoundException("failed to find manuscript")
        val section = sectionRepository.byId(manuscript.sectionId) ?: throw ResourceNotFoundException("failed to find section")
        val publication =  publicationRepository.by(id = section.publicationId) ?: throw ResourceNotFoundException("failed to find publication")
        model["manuscript"] = manuscriptService.toManuscriptDto(manuscript)
        model["publication"] = publication.title
        model["publications"] = publicationRepository.all(manuscriptStateFilter = ManuscriptStateFilter.PUBLISHED).map { it.title }
        model["section"] = section.title
        model["sections"] = sectionRepository.all(
            publicationId = section.publicationId,
            manuscriptStateFilter =  ManuscriptStateFilter.PUBLISHED
        ).map { it.title }
        model["categories"] = categoryRepository.all()
        return "/submit/technical-processing-page"
    }
}