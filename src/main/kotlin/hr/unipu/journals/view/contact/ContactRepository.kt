package hr.unipu.journals.view.contact

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface ContactRepository: Repository<Contact, Int> {
    @Query("SELECT description FROM contact")
    fun description(): String
}
