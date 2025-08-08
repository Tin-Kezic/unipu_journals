package hr.unipu.journals.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("eic_on_publication")
data class EicOnPublication(
    @Id val id: Int? = null,
    val publicationId: Int,
    val eicId: Int,
)