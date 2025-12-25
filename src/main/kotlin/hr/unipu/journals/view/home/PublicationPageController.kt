package hr.unipu.journals.view.home

import hr.unipu.journals.feature.manuscript.category.CategoryRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptStateFilter
import hr.unipu.journals.feature.publication.core.Role
import hr.unipu.journals.feature.publication.core.PublicationRepository
import hr.unipu.journals.feature.publication.core.Sorting
import hr.unipu.journals.feature.section.core.SectionRepository
import hr.unipu.journals.security.AuthorizationService
import hr.unipu.journals.view.ResourceNotFoundException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class PublicationPageController(
    private val publicationRepository: PublicationRepository,
    private val sectionRepository: SectionRepository,
    private val authorizationService: AuthorizationService,
    private val categoryRepository: CategoryRepository,
) {
    @GetMapping("/")
    fun page(
        model: Model,
        @RequestParam publicationId: Int?,
        @RequestParam sectionId: Int?,
        @RequestParam manuscriptStateFilter: ManuscriptStateFilter = ManuscriptStateFilter.PUBLISHED,
        @RequestParam role: Role?,
        @RequestParam category: String?,
        @RequestParam sorting: Sorting = Sorting.ALPHABETICAL_A_Z,
    ): String {
        if(publicationId != null && publicationRepository.exists(publicationId).not()) throw ResourceNotFoundException("failed to find publication $publicationId")
        if(sectionId != null && sectionRepository.exists(sectionId).not()) throw ResourceNotFoundException("failed to find section $sectionId")
        val isAdmin = authorizationService.isAdmin
        model["isAdmin"] = isAdmin
        model["isAuthenticated"] = authorizationService.isAuthenticated
        model["categories"] = categoryRepository.all()
        model["publications"] = publicationRepository.all(
            manuscriptStateFilter = manuscriptStateFilter,
            role = role,
            accountId = authorizationService.account?.id,
            category = category,
            sorting = sorting
        ).map { publication -> buildMap {
            put("id", publication.id)
            put("title", publication.title)
            if(authorizationService.isEicOnPublicationOrAdmin(publication.id)) put("role", "EIC")
        }}
        return "home/publication-page"
    }
}