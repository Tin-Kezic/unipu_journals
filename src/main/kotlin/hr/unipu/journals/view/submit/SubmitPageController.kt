package hr.unipu.journals.view.submit

import hr.unipu.journals.feature.manuscript.category.CategoryRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptStateFilter
import hr.unipu.journals.feature.publication.core.PublicationRepository
import hr.unipu.journals.feature.publication.core.PublicationType
import hr.unipu.journals.feature.section.core.SectionRepository
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_AUTHENTICATED
import hr.unipu.journals.security.AuthorizationService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping

@Controller
class SubmitPageController(
    private val authorizationService: AuthorizationService,
    private val categoryRepository: CategoryRepository,
    private val publicationRepository: PublicationRepository,
) {
    @GetMapping("/submit")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_AUTHENTICATED)
    fun page(model: Model): String {
        model["isAdmin"] = authorizationService.isAdmin
        model["categories"] = categoryRepository.all()
        model["publications"] = publicationRepository.all(ManuscriptStateFilter.PUBLISHED).map { it.title }
        model["sections"] = listOf("Section_1", "Section_2", "Section_3")
        return "submit/submit-page"
    }
}
