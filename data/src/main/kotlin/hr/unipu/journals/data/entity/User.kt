package hr.unipu.journals.data.entity

import hr.unipu.journals.domain.valueobject.Email


data class User(
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