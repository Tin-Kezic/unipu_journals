package hr.unipu.journals.feature.publication.core

import hr.unipu.journals.feature.manuscript.core.ManuscriptStateFilter
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_ADMIN
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_EIC_ON_PUBLICATION_OR_ADMIN
import hr.unipu.journals.security.AuthorizationService
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/publications")
class PublicationController(
    private val publicationRepository: PublicationRepository,
    private val authorizationService: AuthorizationService
) {
    @GetMapping
    fun publications(
        @RequestParam manuscriptStateFilter: ManuscriptStateFilter,
        @RequestParam role: Role?,
        @RequestParam category: String?,
        @RequestParam sorting: Sorting?
    ): List<Map<String, Any>> {
        require(role == null || authorizationService.isAuthenticated) // A -> B
        require(manuscriptStateFilter != ManuscriptStateFilter.ALL_AWAITING_REVIEW || authorizationService.isAuthenticated) // A -> B
        val isAdmin = authorizationService.isAdmin
        if(manuscriptStateFilter == ManuscriptStateFilter.HIDDEN) require(authorizationService.isAdmin)
        return publicationRepository.all(
            manuscriptStateFilter = manuscriptStateFilter,
            role = role,
            accountId = authorizationService.account?.id,
            category = category,
            sorting = sorting ?: Sorting.ALPHABETICAL_A_Z
        ).map { publication -> mapOf(
            "id" to publication.id,
            "title" to publication.title,
            "canHide" to isAdmin,
            "canEdit" to authorizationService.isEicOnPublicationOrAdmin(publication.id),
            "isHidden" to publication.isHidden
        )}
    }
    @PostMapping
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ADMIN)
    fun insert(@RequestParam title: String): ResponseEntity<String> {
        if(title.isEmpty()) return ResponseEntity.badRequest().body("title must not be empty")
        return try {
            val rowsAffected = publicationRepository.insert(Jsoup.clean(title, Safelist.none()))
            if(rowsAffected == 1) ResponseEntity.ok("publication successfully added")
            else ResponseEntity.internalServerError().body("failed to insert publication")
        } catch (e: Exception) {
            if(e.message?.contains("duplicate") ?: false) ResponseEntity.badRequest().body("publication $title already exists")
            else ResponseEntity.internalServerError().body("failed to insert publication")
        }
    }
    @PutMapping("/{publicationId}")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_EIC_ON_PUBLICATION_OR_ADMIN)
    fun update(@PathVariable publicationId: Int, @RequestParam title: String?, @RequestParam isHidden: Boolean?): ResponseEntity<String> {
        return try {
            val rowsAffected = publicationRepository.update(
                publicationId,
                title = title?.run { Jsoup.clean(title, Safelist.none()) },
                isHidden = isHidden
            )
            if(rowsAffected == 1) ResponseEntity.ok("successfully updated publication $publicationId")
            else ResponseEntity.internalServerError().body("failed to update publication $publicationId")
        } catch (e: Exception) {
            if(e.message?.contains("duplicate") ?: false) ResponseEntity.badRequest().body("publication $title already exists")
            else ResponseEntity.internalServerError().body("failed to update publication")
        }
    }
    @DeleteMapping("/{publicationId}")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ADMIN)
    fun delete(@PathVariable publicationId: Int): ResponseEntity<String> {
        val rowsAffected = publicationRepository.delete(publicationId)
        return if(rowsAffected == 1) ResponseEntity.ok("successfully deleted publication $publicationId")
        else ResponseEntity.internalServerError().body("failed to delete publication $publicationId")
    }
}