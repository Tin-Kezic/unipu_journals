package hr.unipu.journals.data.domain.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("eic_on_publication")
data class EicOnPublication(
    @Id val id: Int,
    val publicationId: Int,
    val eicId: Int,
)