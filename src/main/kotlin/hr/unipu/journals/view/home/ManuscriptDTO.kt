package hr.unipu.journals.view.home

data class ManuscriptDTO (
    val id: Int,
    val title: String,
    val authors: String,
    val downloadUrl: String,
    val submissionDate: String,
    val publicationDate: String,
    val description: String,
)