package hr.unipu.journals.view.home

data class ContainerDTO (
    val id: Int,
    val title: String,
    val canHide: Boolean,
    val canEdit: Boolean,
)