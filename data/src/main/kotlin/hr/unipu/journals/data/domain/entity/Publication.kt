package hr.unipu.journals.data.domain.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("publication")
data class Publication(
    @Id val id: Int,
    val title: String,
    val isHidden: Boolean,
    val dateOfCreation: LocalDateTime,
)