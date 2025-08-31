package hr.unipu.journals.feature.publication

import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_ADMIN
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_EIC_ON_PUBLICATION_OR_SUPERIOR
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/publication")
class PublicationController(private val repository: PublicationRepository) {

    @PostMapping("/insert")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ADMIN)
    fun insert(@RequestParam title: String): ResponseEntity<Any> {
        return if(title.isNotEmpty()) {
            repository.insert(Jsoup.clean(title, Safelist.none()))
            ResponseEntity.ok(repository.byTitle(title))
        } else ResponseEntity.badRequest().body("title must not be empty")
    }
    @PutMapping("/update-title")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_EIC_ON_PUBLICATION_OR_SUPERIOR)
    fun updateTitle(@PathVariable publicationId: Int, @RequestParam title: String): ResponseEntity<String> {
        return if(repository.existsById(publicationId)) {
            repository.updateTitle(publicationId, Jsoup.clean(title, Safelist.none()))
            ResponseEntity.ok("title successfully updated")
        } else ResponseEntity.badRequest().body("publication with id: $publicationId does not exist")

    }
    @PutMapping("/hide/{publicationId}")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_EIC_ON_PUBLICATION_OR_SUPERIOR)
    fun updateHidden(@PathVariable publicationId: Int, @RequestParam isHidden: Boolean): ResponseEntity<String> {
        return if (repository.existsById(publicationId)) {
            repository.updateHidden(publicationId, isHidden)
            ResponseEntity.ok("publication successfully hidden")
        } else ResponseEntity.badRequest().body("id does not exist")
    }
}