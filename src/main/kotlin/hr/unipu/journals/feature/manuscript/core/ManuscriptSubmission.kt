package hr.unipu.journals.feature.manuscript.core

data class ManuscriptSubmission(
    val title: String,
    val category: String,
    val publicationName: String,
    val sectionName: String,
    val authors: List<AuthorDTO>,
    val correspondingAuthorEmail: String,
    val description: String,
)