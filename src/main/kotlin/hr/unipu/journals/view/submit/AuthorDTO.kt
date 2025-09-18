package hr.unipu.journals.view.submit

data class AuthorDTO(
    val fullName: String,
    val institutionalEmail: String,
    val country: String,
    val affiliation: String,
    val isCorrespondingAuthor: Boolean
)
