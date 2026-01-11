package hr.unipu.journals.feature.manuscript.file

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("manuscript_file")
data class ManuscriptFile(
    @Id val id: Int,
    val path: String,
    val manuscriptId: Int
)