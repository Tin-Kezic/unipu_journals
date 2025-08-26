package hr.unipu.journals.view

import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_ROOT
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class RootPageController() {
    @GetMapping("/root")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_ROOT)
    fun page() = "configure/root-page"
}