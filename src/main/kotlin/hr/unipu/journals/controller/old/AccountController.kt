package hr.unipu.journals.controller.old

import hr.unipu.journals.data.entity.Account
import hr.unipu.journals.data.repository.AccountRepository
import hr.unipu.journals.usecase.hashPassword
import hr.unipu.journals.usecase.sanitize
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/api/account")
class AccountController(private val repository: AccountRepository) {

    private fun processAccount(account: Account): Account {
        return account.copy(
            id = null,
            name = sanitize(account.name),
            surname = sanitize(account.surname),
            title = sanitize(account.title),
            email = sanitize(account.email), // implement check if email is already present in database
            password = hashPassword(account.password),
            affiliation = sanitize(account.affiliation),
            jobType = sanitize(account.jobType),
            country = sanitize(account.country),
            city = sanitize(account.city),
            address = sanitize(account.address),
            zipCode = sanitize(account.zipCode)
        )
    }
    @ResponseBody
    @PostMapping("/save")
    fun save(@ModelAttribute account: Account): ResponseEntity<String> {
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
    @PostMapping("/saveAll")
    fun saveAll(@ModelAttribute accounts: List<Account>): ResponseEntity<String> {
        return try {
            val sanitizedAccounts = accounts.map { processAccount(it) }
            repository.saveAll(sanitizedAccounts)
            ResponseEntity.ok().body("accounts successfully saved")
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().body("Invalid account data. All fields must be non-null. Provided: $accounts")
        } catch (_: OptimisticLockingFailureException) {
            ResponseEntity.internalServerError().body("internal server error. OptimisticLockingFailureException")
        }
    }
    @GetMapping("/{id}")
    fun findById(@PathVariable id: Int, model: Model): String {
        val account = repository.findById(id).orElse(null) // change later cause mustache crashes on null
        model["account"] = account
        return "partial/account"
    }
    @ResponseBody
    @GetMapping("/exists/{id}")
    fun existsById(@PathVariable id: Int): Boolean {
        return try {
            repository.existsById(id)
        } catch (_: IllegalArgumentException) { false }
    }

    @GetMapping("/all")
    fun findAll(model: Model): String {
        model["accounts"] = repository.findAll()
        return "partial/accounts"
    }
    @PostMapping("/findAllById")
    fun findAllById(@ModelAttribute ids: List<Int>, model: Model): String {
        val found = repository.findAllById(ids)
        model["accounts"] = found
        return "partial/accounts"
    }
    @ResponseBody
    @GetMapping("/count")
    fun count() = repository.count()

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
    @ResponseBody
    @PostMapping("/delete")
    fun delete(@ModelAttribute account: Account): ResponseEntity<String> {
        return try {
            repository.delete(account)
            ResponseEntity.ok().body("account deleted successfully")
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().body("Invalid account data. ID must be non-null")
        } catch (_: OptimisticLockingFailureException) {
            ResponseEntity.internalServerError().body("internal server error. OptimisticLockingFailureException")
        }
    }
    @ResponseBody
    @PostMapping("/deleteAllById")
    fun deleteAllById(@ModelAttribute ids: List<Int>): ResponseEntity<String> {
        return try {
            repository.deleteAllById(ids)
            ResponseEntity.ok().body("account deleted successfully")
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().body("Invalid account data. ID must be non-null")
        } catch (_: OptimisticLockingFailureException) {
            ResponseEntity.internalServerError().body("internal server error. OptimisticLockingFailureException")
        }
    }

    @ResponseBody
    @PostMapping("/deleteAllEntities")
    fun deleteAllEntities(@ModelAttribute accounts: List<Account>): ResponseEntity<String> {
        return try {
            repository.deleteAll(accounts)
            ResponseEntity.ok().body("accounts deleted successfully")
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().body("Invalid account data. IDs must be non-null")
        } catch (_: OptimisticLockingFailureException) {
            ResponseEntity.internalServerError().body("internal server error. OptimisticLockingFailureException")
        }
    }

    @ResponseBody
    @PostMapping("/deleteAll")
    fun deleteAll() = repository.deleteAll()
}