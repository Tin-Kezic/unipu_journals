package hr.unipu.journals.data.domain.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("publication_section")
data class PublicationSection(
    @Id val id: Int,
    val title: String,
    val description: String,
    val publicationId: Int,
    val isHidden: Boolean,
    val dateOfCreation: LocalDateTime,
)
