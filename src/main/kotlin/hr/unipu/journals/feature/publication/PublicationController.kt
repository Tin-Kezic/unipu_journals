package hr.unipu.journals.feature.publication

import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.http.ResponseEntity
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
    fun insert(@ModelAttribute title: String): ResponseEntity<String> {
        return if(title.isNotEmpty()) {
            repository.insert(Jsoup.clean(title, Safelist.none()))
            ResponseEntity.ok().body("account successfully added")
        } else ResponseEntity.badRequest().body("title must not be empty")
    }
    @PutMapping("/update")
    fun updateTitle(@ModelAttribute id: Int, @ModelAttribute title: String): ResponseEntity<String> {
        return if(repository.existsById(id)) {
            repository.updateTitle(id, Jsoup.clean(title, Safelist.none()))
            ResponseEntity.ok().body("title successfully updated")
        } else ResponseEntity.badRequest().body("publication with id: $id does not exist")
    }
    @PutMapping("/hide/{publicationId}")
    fun updateHidden(
        @PathVariable publicationId: Int,
        @RequestParam isHidden: Boolean
    ): ResponseEntity<String> {
        return if (repository.existsById(publicationId)) {
            repository.updateHidden(publicationId, isHidden)
            ResponseEntity.ok().body("publication successfully hidden")
        } else ResponseEntity.badRequest().body("id does not exist")
    }
}