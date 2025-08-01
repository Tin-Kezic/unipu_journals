package hr.unipu.journals.data.domain.entity

import hr.unipu.journals.data.domain.type.AccountRole
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("account_role_on_manuscript")
data class AccountRoleOnManuscript(
    @Id val id: Int,
    val manuscriptId: Int,
    val accountId: String,
    @Column("current_account_role")
    val accountRole: AccountRole
)
