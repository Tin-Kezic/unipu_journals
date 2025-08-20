package hr.unipu.journals.feature.account

/*
@RestController
@RequestMapping("/api/account")
class AccountController(private val repository: AccountRepository) {

    private fun processAccount(account: Account): Account {
        return account.copy(
            name = sanitize(account.name),
            surname = sanitize(account.surname),
            title = sanitize(account.title),
            email = sanitize(account.email),
            password = BCryptPasswordEncoder().encode(password)
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
    /*
    @GetMapping("/{id}/{template}")
    fun findById(@PathVariable id: Int, model: Model): String {
        val account = repository.findById(id).orElse(null) // change later cause mustache crashes on null
        model["account"] = account
        return "partial/account"
    }
     */
    /*
    @GetMapping("/all")
    fun findAll(model: Model, ): String {
        model["accounts"] = repository.findAll()
        return "partial/accounts"
    }
    @PostMapping("/findAllById")
    fun findAllById(@ModelAttribute ids: List<Int>, model: Model): String {
        val found = repository.findAllById(ids)
        model["accounts"] = found
        return "partial/accounts"
    }
     */

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
 */