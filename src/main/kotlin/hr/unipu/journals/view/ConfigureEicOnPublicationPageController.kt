package hr.unipu.journals.view

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ConfigureEicOnPublicationPageController() {
    @GetMapping("/publication/{publicationId}/manage-eic-on-publication")
    fun page() = "manage/manage-eic-on-publication"
}
