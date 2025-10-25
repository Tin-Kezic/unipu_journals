package hr.unipu.journals.view.submit

import hr.unipu.journals.feature.manuscript.category.CategoryRepository
import hr.unipu.journals.feature.publication.core.PublicationRepository
import hr.unipu.journals.feature.section.core.SectionRepository
import hr.unipu.journals.security.AuthorizationService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping

@Controller
class SubmitPageController(
    private val authorizationService: AuthorizationService,
    private val categoryRepository: CategoryRepository,
    private val publicationRepository: PublicationRepository,
    private val sectionRepository: SectionRepository
) {
    @GetMapping("/submit")
    fun page(model: Model): String {
        model["isAdmin"] = authorizationService.isAdmin()
        model["categories"] = categoryRepository.all()
        model["publications"] = publicationRepository.allPublished().map { it.title }
        model["sections"] = listOf("Section_1", "Section_2", "Section_3")
        return "submit/submit-page"
    }
}
