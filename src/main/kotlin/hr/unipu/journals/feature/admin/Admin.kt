package hr.unipu.journals.feature.admin

import org.springframework.data.relational.core.mapping.Table

@Table("admin")
data class Admin(
    val id: Int,
    val email: String
)