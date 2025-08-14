package hr.unipu.journals.security

import hr.unipu.journals.feature.account.AccountRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

class CustomUserDetailsService(
    private val accountRepository: AccountRepository,
    private val roleContextService: RoleContextService
): UserDetailsService {
    override fun loadUserByUsername(email: String): UserDetails {
        if(!accountRepository.emailExists(email)) throw UsernameNotFoundException("$email not found")

        val account = accountRepository.byEmail(email)
        val authority = mutableListOf<GrantedAuthority>()

        if(account.isAdmin) authority.add(SimpleGrantedAuthority(Role.ADMIN.name))
    }
}