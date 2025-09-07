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
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException

private const val ROLE_ROOT = "ROLE_ROOT"
private const val ROLE_ADMIN = "ROLE_ADMIN"
private const val ROOT = "ROOT"
private const val ADMIN = "ADMIN"
private const val EIC = "EIC"
private const val SECTION_EDITOR = "SECTION_EDITOR"
private const val EDITOR = "EDITOR"
private const val REVIEWER = "REVIEWER"
private const val CORRESPONDING_AUTHOR = "CORRESPONDING_AUTHOR"
private const val AUTHOR = "AUTHOR"

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {
    @Bean
    fun userDetailsService(accountRepository: AccountRepository): UserDetailsService {
        return UserDetailsService { email ->
            val account = accountRepository.byEmail(email) ?: throw UsernameNotFoundException("User with email $email not found")
            if (account.email == "root@unipu.hr") {
                User(account.email, account.password, listOf(SimpleGrantedAuthority(ROLE_ROOT)))
            } else {
                val authorities = mutableListOf<GrantedAuthority>()
                if (account.isAdmin) authorities.add(SimpleGrantedAuthority(ROLE_ADMIN))
                User(account.email, account.password, authorities)
            }
        }
    }
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(12)

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() } // comment out in production
            headers { frameOptions { disable() } } // comment out in production
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
                invalidSessionUrl = "/"
                invalidSessionUrl = "/login.html?invalidSession"
            }
            authorizeHttpRequests {
                authorize("/root", hasRole(ROOT))
                listOf(
                    "/hidden/**",
                    "/publication/{publicationId}/manage-eic-on-publication",
                ).forEach { authorize(it, hasRole(ADMIN)) }
                listOf(
                    "/eic-initial-review",
                    "/technical-processing-page"
                ).forEach { authorize(it, hasAnyRole(EIC, ADMIN)) }
                listOf(
                    "/review-round-initialization",
                    "/manage-manuscript-under-review",
                ).forEach { authorize(it, hasAnyRole(EDITOR, SECTION_EDITOR, EIC, ADMIN)) }
                listOf(
                    "/review",
                    "/review/{manuscriptReviewId}",
                ).forEach { authorize(it, hasAnyRole(REVIEWER, EDITOR, SECTION_EDITOR, EIC, ADMIN)) }
                listOf(
                    "/submit",
                    "/review/**",
                    "/profile/{accountId}",
                    "/profile/{accountId}/edit",
                    "/api/publication/{publicationId}/section/{sectionId}/insert"
                ).forEach { authorize(it, authenticated) }
                authorize(anyRequest, permitAll)
            }
        }
        return http.build()
    }
}