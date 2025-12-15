package hr.unipu.journals.feature.manuscript.core

import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.ManuscriptRole
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime

data class AffiliatedManuscript(
    val accountRole: ManuscriptRole,
    @Id val id: Int,
    val title: String,
    val description: String,
    val categoryId: Int,
    @Column("current_state")
    val state: ManuscriptState,
    val sectionId: Int,
    val downloadUrl: String,
    val submissionDate: LocalDateTime,
    val publicationDate: LocalDateTime?,
)