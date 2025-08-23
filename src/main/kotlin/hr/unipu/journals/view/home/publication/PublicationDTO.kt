package hr.unipu.journals.view.home.publication

data class PublicationDTO (
    val id: Int,
    val title: String,
    val canHide: Boolean,
    val canEdit: Boolean,
    val isEic: Boolean
)