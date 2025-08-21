package hr.unipu.journals.feature.account

import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/account")
class AccountController(private val repository: AccountRepository) {

    @PostMapping("/save")
    fun save(
        @ModelAttribute fullName: String,
        @ModelAttribute title: String,
        @ModelAttribute email: String,
        @ModelAttribute password: String,
        @ModelAttribute passwordConfirmation: String,
        @ModelAttribute affiliation: String,
        @ModelAttribute jobType: String,
        @ModelAttribute country: String,
        @ModelAttribute city: String,
        @ModelAttribute address: String,
        @ModelAttribute zipcode: String,
    ): ResponseEntity<String> {
        return try {
            repository.save(processAccount(account))
            ResponseEntity.ok().body("account ${account.name} ${account.surname} successfully saved")
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().body("Invalid account data. All fields must be non-null. Provided: $account")
        } catch (_: OptimisticLockingFailureException) {
            ResponseEntity.internalServerError().body("internal server error of type OptimisticLockingFailureException")
        }
    }
    @ResponseBody
    @PostMapping("/deleteById/{id}")
    fun deleteById(@PathVariable id: Int) {
        try {
            repository.deleteById(id)
            ResponseEntity.ok().body("account deleted successfully")
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().body("Invalid account data. ID must be non-null")
        } catch (_: OptimisticLockingFailureException) {
            ResponseEntity.internalServerError().body("internal server error. OptimisticLockingFailureException")
        }
    }
}