package hr.unipu.journals.feature.publication

import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_ADMIN
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_EIC_ON_PUBLICATION_OR_SUPERIOR
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/publication/{publicationId}")
class PublicationController(private val publicationRepository: PublicationRepository) {
    @PostMapping("/insert")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ADMIN)
    fun insert(@RequestParam title: String): ResponseEntity<String> {
        if(title.isEmpty()) return ResponseEntity.badRequest().body("title must not be empty")
        val rowsAffected = publicationRepository.insert(Jsoup.clean(title, Safelist.none()))
        return if(rowsAffected == 1) ResponseEntity.ok("publication successfully added")
        else ResponseEntity.internalServerError().body("failed to insert publication")
    }
    @PutMapping("/update-title")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_EIC_ON_PUBLICATION_OR_SUPERIOR)
    fun updateTitle(@PathVariable publicationId: Int, @RequestParam title: String): ResponseEntity<String> {
        val rowsAffected = publicationRepository.updateTitle(publicationId, Jsoup.clean(title, Safelist.none()))
        return if(rowsAffected == 1) ResponseEntity.ok("publication $publicationId title successfully updated to $title")
        else ResponseEntity.internalServerError().body("failed to update publication $publicationId")
    }
    @PutMapping("/update-hidden")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ADMIN)
    fun updateHidden(@PathVariable publicationId: Int, @RequestParam isHidden: Boolean): ResponseEntity<String> {
        val rowsAffected = publicationRepository.updateHidden(publicationId, isHidden)
        return if(rowsAffected == 1) ResponseEntity.ok("publication hidden status successfully updated to $isHidden")
        else ResponseEntity.internalServerError().body("failed to update hidden status on publication $publicationId")
    }
    @DeleteMapping("/{publicationId}/delete")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ADMIN)
    fun delete(@PathVariable publicationId: Int): ResponseEntity<String> {
        val rowsAffected = publicationRepository.delete(publicationId)
        return if(rowsAffected == 1) ResponseEntity.ok("successfully deleted publication $publicationId")
        else ResponseEntity.internalServerError().body("failed to delete publication $publicationId")
    }
}