package hr.unipu.journals.feature.manuscript

data class InsertManuscriptDTO (
    val title: String,
    val authorId: Int,
    val categoryId: Int,
    val fileUrl: String
)