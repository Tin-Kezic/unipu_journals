package hr.unipu.journals.view

import hr.unipu.journals.security.AuthorizationService
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

private var contacts = ""

@Controller
@RequestMapping("/contacts")
class ContactPageController(private val authorizationService: AuthorizationService) {
    @GetMapping
    fun page(model: Model): String {
        model["contacts"] = contacts
        model["isAdmin"] = authorizationService.account?.isAdmin
        return "contact"
    }
    @PutMapping
    fun update(@RequestParam info: String): ResponseEntity<String> {
        contacts = Jsoup.clean(info, Safelist.relaxed())
        return ResponseEntity.ok("successfully updated contacts")
    }
}