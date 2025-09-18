package hr.unipu.journals.feature.profile

data class ProfileRequestDTO(
    val fullName: String,
    val title: String,
    val email: String,
    val password: String,
    val passwordConfirmation: String,
    val affiliation: String,
    val jobType: String,
    val country: String,
    val city: String,
    val address: String,
    val zipCode: String,
)