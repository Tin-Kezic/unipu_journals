package hr.unipu.journals.feature.publication.core

import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.AccountRoleOnManuscript
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.ManuscriptRole
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_ADMIN
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_EIC_ON_PUBLICATION_OR_SUPERIOR
import hr.unipu.journals.security.AuthorizationService
import hr.unipu.journals.view.home.ContainerDTO
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
@RequestMapping("/api/publication")
class PublicationController(
    private val publicationRepository: PublicationRepository,
    private val authorizationService: AuthorizationService
) {
    @GetMapping
    fun publications(
        @RequestParam publicationType: PublicationType,
        @RequestParam affiliation: ManuscriptRole
    ): List<ContainerDTO> {
        val isAdmin = authorizationService.isAdmin
        return publicationRepository.all(PublicationType.PUBLIC).map { publication ->
            val isEicOrSuperior = authorizationService.isEicOnPublicationOrSuperior(publication.id)
            ContainerDTO(
                id = publication.id,
                title = publication.title,
                canHide = isAdmin,
                canEdit = isEicOrSuperior,
            )
        }
    }
    @PostMapping
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ADMIN)
    fun insert(@RequestParam title: String): ResponseEntity<String> {
        if(title.isEmpty()) return ResponseEntity.badRequest().body("title must not be empty")
        val rowsAffected = publicationRepository.insert(Jsoup.clean(title, Safelist.none()))
        return if(rowsAffected == 1) ResponseEntity.ok("publication successfully added")
        else ResponseEntity.internalServerError().body("failed to insert publication")
    }
    @PutMapping("/{publicationId}")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_EIC_ON_PUBLICATION_OR_SUPERIOR)
    fun update(@PathVariable publicationId: Int, @RequestParam title: String?, @RequestParam isHidden: Boolean?): ResponseEntity<String> {
        val rowsAffected = publicationRepository.update(
            publicationId,
            title = title?.run { Jsoup.clean(title, Safelist.none()) },
            isHidden = isHidden
        )
        return if(rowsAffected == 1) ResponseEntity.ok("successfully updated publication $publicationId")
        else ResponseEntity.internalServerError().body("failed to update publication $publicationId")
    }
    @DeleteMapping("/{publicationId}")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ADMIN)
    fun delete(@PathVariable publicationId: Int): ResponseEntity<String> {
        val rowsAffected = publicationRepository.delete(publicationId)
        return if(rowsAffected == 1) ResponseEntity.ok("successfully deleted publication $publicationId")
        else ResponseEntity.internalServerError().body("failed to delete publication $publicationId")
    }
}