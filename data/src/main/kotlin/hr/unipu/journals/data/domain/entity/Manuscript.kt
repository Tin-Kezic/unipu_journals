package hr.unipu.journals.data.domain.entity

import hr.unipu.journals.data.domain.type.ManuscriptState
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("manuscript")
data class Manuscript(
    @Id val id: Int,
    val authorId: Int,
    val categoryId: Int,
    val state: ManuscriptState,
    val publicationSectionId: Int,
    val fileUrl: String, // maps to URL
    val submissionDate: LocalDateTime,
    val round: Int = 1,
    val downloads: Int = 0,
    val views: Int = 0,
)