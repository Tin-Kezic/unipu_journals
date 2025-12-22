package hr.unipu.journals.feature.unregistered_author

import org.springframework.data.annotation.Id

data class UnregisteredAuthor(
    @Id val id: String,
    val fullName: String,
    val email: String,
    val country: String,
    val affiliation: String,
    val manuscriptId: Int
)
