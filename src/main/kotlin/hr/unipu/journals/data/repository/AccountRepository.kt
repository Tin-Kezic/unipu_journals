package hr.unipu.journals.data.repository

import hr.unipu.journals.data.entity.Account
import org.springframework.data.repository.CrudRepository

interface AccountRepository: CrudRepository<Account, Int> {

}
