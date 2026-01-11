package hr.unipu.journals.feature.manuscript.core

import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.ManuscriptRole
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime

data class AccountRolesAndManuscript(
    val roles: List<ManuscriptRole>,
    @Id val id: Int,
    val title: String,
    val description: String,
    val categoryId: Int,
    @Column("current_state")
    val state: ManuscriptState,
    val sectionId: Int,
    val submissionDate: LocalDateTime,
    val publicationDate: LocalDateTime?,
    val correspondingAuthorEmail: String
)