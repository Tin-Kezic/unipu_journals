package hr.unipu.journals.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("publication")
data class Publication(
    @Id val id: Int,
    val title: String,
    val isHidden: Boolean,
)