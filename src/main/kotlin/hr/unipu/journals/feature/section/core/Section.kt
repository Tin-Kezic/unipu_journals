package hr.unipu.journals.feature.section.core

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("publication_section")
data class Section(
    @Id val id: Int,
    val title: String,
    val description: String?,
    val publicationId: Int,
    val isHidden: Boolean,
)