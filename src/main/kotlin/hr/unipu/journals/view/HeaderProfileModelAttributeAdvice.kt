package hr.unipu.journals.view

import hr.unipu.journals.security.AuthorizationService
import hr.unipu.journals.view.home.PublicationPageController
import hr.unipu.journals.view.profile.EditProfilePageController
import hr.unipu.journals.view.profile.ProfilePageController
import hr.unipu.journals.view.review.EicInitialReviewPageController
import hr.unipu.journals.view.review.ReviewPageController
import hr.unipu.journals.view.review.ReviewRoundInitializationPageController
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
    ReviewPageController::class,
    ReviewRoundInitializationPageController::class,
    SubmitPageController::class,
    TechnicalProcessingPageController::class,
])
class HeaderProfileModelAttributeAdvice(private val authorizationService: AuthorizationService) {
    @ModelAttribute
    fun user(model: Model) {
        val account = authorizationService.account
        if(account == null) {
            model["is-logged-in"] = false
            return
        }
        model["is-logged-in"] = true
        model["header-profile-id"] = account.id
        model["header-profile-full-name"] = account.fullName
        model["header-profile-title"] = account.title
    }
}