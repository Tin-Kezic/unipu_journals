package hr.unipu.journals.feature.manuscript

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("manuscript")
data class Manuscript(
    @Id val id: Int,
    val title: String,
    val description: String,
    val authorId: Int,
    val categoryId: Int,
    @Column("current_state")
    val state: ManuscriptState,
    val sectionId: Int,
    val fileUrl: String,
    val submissionDate: LocalDateTime,
    val publicationDate: LocalDateTime?,
    val views: Int,
    val downloads: Int,
)