package hr.unipu.journals.feature.manuscript.category

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("category")
data class Category(
    @Id val id: Int,
    val name: String,
)