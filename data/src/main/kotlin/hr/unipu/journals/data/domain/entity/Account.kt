package hr.unipu.journals.data.domain.entity

import hr.unipu.journals.domain.valueobject.Email
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("account")
data class Account(
    @Id
    val id: Int = 0,
    val name: String,
    val surname: String,
    val title: String?,
    val email: Email,
    val password: String,
    val affiliation: String,
    val jobType: String,
    val country: String,
    val city: String,
    val address: String,
    val zipCode: String,
)