package hr.unipu.journals.data.entity

import hr.unipu.journals.data.enumeration.ManuscriptState
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("manuscript")
data class Manuscript(
    @Id val id: Int? = null,
    val authorId: Int,
    val categoryId: Int,
    val state: ManuscriptState,
    val publicationSectionId: Int,
    val fileUrl: String,
    val submissionDate: LocalDateTime,
    val publicationDate: LocalDateTime,
    val downloads: Int,
    val views: Int,
)