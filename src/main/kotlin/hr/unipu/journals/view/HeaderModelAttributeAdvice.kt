package hr.unipu.journals.view

import hr.unipu.journals.security.AuthorizationService
import hr.unipu.journals.view.archive.ArchiveManuscriptPageController
import hr.unipu.journals.view.archive.ArchivePublicationPageController
import hr.unipu.journals.view.archive.ArchiveSectionPageController
import hr.unipu.journals.view.contact.ContactPageController
import hr.unipu.journals.view.hidden.HiddenManuscriptPageController
import hr.unipu.journals.view.hidden.HiddenPublicationPageController
import hr.unipu.journals.view.hidden.HiddenSectionPageController
import hr.unipu.journals.view.home.ManuscriptDetailsPageController
import hr.unipu.journals.view.home.ManuscriptPageController
import hr.unipu.journals.view.home.SectionPageController
import hr.unipu.journals.view.home.publication.PublicationPageController
import hr.unipu.journals.view.profile.EditProfilePageController
import hr.unipu.journals.view.profile.ProfilePageController
import hr.unipu.journals.view.review.EicInitialReviewPageController
import hr.unipu.journals.view.review.ManageManuscriptUnderReviewPageController
import hr.unipu.journals.view.review.PendingReviewPageController
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
    ArchiveManuscriptPageController::class,
    ArchivePublicationPageController::class,
    ArchiveSectionPageController::class,
    ContactPageController::class,
    HiddenManuscriptPageController::class,
    HiddenPublicationPageController::class,
    HiddenSectionPageController::class,
    ManuscriptDetailsPageController::class,
    ManuscriptPageController::class,
    PublicationPageController::class,
    SectionPageController::class,
    EditProfilePageController::class,
    ProfilePageController::class,
    EicInitialReviewPageController::class,
    ManageManuscriptUnderReviewPageController::class,
    PendingReviewPageController::class,
    ReviewPageController::class,
    ReviewRoundInitializationPageController::class,
    SubmitPageController::class,
    TechnicalProcessingPageController::class,
])
class HeaderModelAttributeAdvice(private val authorizationService: AuthorizationService) {

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