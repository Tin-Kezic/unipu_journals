package hr.unipu.journals.view.submit

import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_EIC_ON_MANUSCRIPT_OR_SUPERIOR
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@PreAuthorize(AUTHORIZATION_SERVICE_IS_EIC_ON_MANUSCRIPT_OR_SUPERIOR)
@RequestMapping("/manuscripts/{manuscriptId}/technical-processing")
class TechnicalProcessingPageController {
    @GetMapping
    fun page(): String {
        return "/submit/technical-processing-page"
    }
}