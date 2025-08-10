package hr.unipu.journals.feature.account

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("ACCOUNT")
data class Account(
    @Id val id: Int,
    val name: String,
    val surname: String,
    val title: String?,
    val email: String,
    val password: String,
    val affiliation: String,
    val jobType: String,
    val country: String,
    val city: String,
    val address: String,
    val zipCode: String,
    val isAdmin: Boolean,
)