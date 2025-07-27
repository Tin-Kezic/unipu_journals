package hr.unipu.journals.data.entity

import hr.unipu.journals.domain.valueobject.Email
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("account")
data class Account(
    @Id val id: Long = 0,
    val name: String,
    val surname: String,
    val jobType: String,
    val title: String,
    val mail: Email,
    val password: String,
    val affiliation: String,
    val address: String,
    val zipcode: String,
    val city: String,
    val country: String
)