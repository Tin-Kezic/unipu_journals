package hr.unipu.journals.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("publication_section")
data class PublicationSection(
    @Id val id: Int? = null,
    val title: String,
    val description: String?,
    val publicationId: Int,
    val isHidden: Boolean,
)