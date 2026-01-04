package hr.unipu.journals.security

import hr.unipu.journals.feature.account.AccountRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {
    @Bean fun userDetailsService(accountRepository: AccountRepository) = UserDetailsService { email ->
        val account = accountRepository.byEmail(email) ?: throw UsernameNotFoundException("User with email $email not found")
        User(account.email, account.password, listOf<GrantedAuthority>())
    }
    @Bean fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(12)
    @Bean fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() } // todo. comment out in production
            headers { frameOptions { disable() } } // todo. comment out in production
            formLogin {
                loginPage = "/login.html"
                loginProcessingUrl = "/login"
                defaultSuccessUrl("/", false)
                failureUrl = "/login.html?failure"
                permitAll()
            }
            logout { // default: logoutUrl = "/logout"
                logoutSuccessUrl = "/"
                deleteCookies("JSESSIONID")
                invalidateHttpSession = true
            }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.IF_REQUIRED
                invalidSessionUrl = "/login.html?invalidSession"
            }
            authorizeHttpRequests { authorize(anyRequest, permitAll) }
        }
        return http.build()
    }
}