package hr.unipu.journals.view

import hr.unipu.journals.security.AuthorizationService
import hr.unipu.journals.view.home.PublicationPageController
import hr.unipu.journals.view.profile.EditProfilePageController
import hr.unipu.journals.view.profile.ProfilePageController
import hr.unipu.journals.view.review.EicInitialReviewPageController
import hr.unipu.journals.view.submit.SubmitPageController
import hr.unipu.journals.view.submit.TechnicalProcessingPageController
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

// Because @ModelAttribute methods in @ControllerAdvice execute before every handler,
// limit usage to controllers whose handlers render views with headers.
@ControllerAdvice(assignableTypes = [
    ContactPageController::class,
    PublicationPageController::class,
    EditProfilePageController::class,
    ProfilePageController::class,
    EicInitialReviewPageController::class,
    SubmitPageController::class,
    TechnicalProcessingPageController::class,
    SearchPageController::class
])
class HeaderProfileModelAttributeAdvice(private val authorizationService: AuthorizationService) {
    @ModelAttribute
    fun user(model: Model) {
        val account = authorizationService.account
        if(account == null) {
            model["header-is-authenticated"] = false
            return
        }
        model["header-is-authenticated"] = true
        model["header-profile-id"] = account.id
        model["header-profile-full-name"] = account.fullName
        model["header-profile-title"] = account.title
    }
}