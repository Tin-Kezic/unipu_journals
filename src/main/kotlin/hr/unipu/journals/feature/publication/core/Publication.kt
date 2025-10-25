package hr.unipu.journals.feature.publication.core

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("publication")
data class Publication(
    @Id val id: Int,
    val title: String,
    val isHidden: Boolean,
)