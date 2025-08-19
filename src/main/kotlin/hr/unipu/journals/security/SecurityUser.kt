package hr.unipu.journals.security

import hr.unipu.journals.feature.account.Account
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class SecurityUser(
    private val account: Account,
    private val authority: Collection<GrantedAuthority>
): UserDetails {
    override fun getAuthorities() = authority
    override fun getPassword() = account.password
    override fun getUsername() = account.email
    val id get() = account.id
    val isAdmin get() = account.isAdmin

}