package hr.unipu.journals.view.profile

data class ProfileManuscriptDTO (
    val id: Int,
    val title: String,
    val authors: List<String>,
    val publicationDate: String,
    val description: String,
)