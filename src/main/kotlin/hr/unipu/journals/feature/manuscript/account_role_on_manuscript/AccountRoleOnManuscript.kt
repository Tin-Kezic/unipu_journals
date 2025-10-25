package hr.unipu.journals.feature.manuscript.account_role_on_manuscript

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("account_role_on_manuscript")
data class AccountRoleOnManuscript(
    @Id val id: Int,
    val manuscriptId: Int,
    val accountId: Int,
    @Column("current_role")
    val accountRole: ManuscriptRole
)