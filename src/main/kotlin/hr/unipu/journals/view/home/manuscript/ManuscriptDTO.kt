package hr.unipu.journals.view.home.manuscript

data class ManuscriptDTO (
    val id: Int,
    val title: String,
    val authors: List<String>,
    val fileUrl: String,
    val publicationDate: String,
    val description: String,
)