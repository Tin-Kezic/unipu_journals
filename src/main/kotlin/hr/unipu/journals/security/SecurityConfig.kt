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
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.userdetails.UserDetails

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {
    @Bean
    fun root(): UserDetailsService = InMemoryUserDetailsManager(
        listOf<UserDetails>(
            User
                .withUsername("root@unipu.hr")
                .password("\$2a\$10\$rEygfn5AFuDbSFDQasv/h.xf2YptMtlhap8sD7vyIQwS4bj39XOzy") // replace with actual bcrypt password in production
                .roles(ROOT)
                .build(),
            User // comment out in production
                .withUsername("admin@unipu.hr")
                .password("\$2a\$10\$RcMJcymGto39rp7ys9PSdu3taGabj.26v2MRdWFSQ3FtY2O1Nw1Yy")
                .roles(ADMIN)
                .build(),
        )
    )
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity/*, accountRepository: AccountRepository*/): SecurityFilterChain {
        http {
            csrf { disable() } // comment out in production
            headers { frameOptions { disable() } } // comment out in production
            formLogin {
                loginPage = "/login.html"
                loginProcessingUrl = "/login"
                defaultSuccessUrl("/", true)
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
            /*
            UserDetailsService { email ->
                if(!accountRepository.emailExists(email)) throw UsernameNotFoundException("$email not found")
                val account = accountRepository.byEmail(email)
                SecurityUser(account, if(account.isAdmin) listOf(SimpleGrantedAuthority(ADMIN)) else emptyList())
            }
             */
            authorizeHttpRequests {
                authorize("h2-console/**", permitAll) // comment out in production
                authorize("/root", hasRole(ROOT))
                authorize("/publication/{publicationId}/configure-eic-on-publication", hasRole(ADMIN))
                listOf(
                    "/eic-initial-review",
                    "/technical-processing-page"
                ).forEach { authorize(it, hasAnyRole(EIC, ADMIN)) }
                listOf(
                    "/hidden",
                    "/hidden/publication/{publicationId}",
                    "/hidden/publication/{publicationId}/section/{sectionId}",
                    "/hidden/publication/{publicationId}/section/{sectionId}/manuscript/{manuscriptId}",
                    "/review-round-initialization",
                    "/manage-manuscript-under-review",
                ).forEach { authorize(it, hasAnyRole(EDITOR, SECTION_EDITOR, EIC, ADMIN)) }
                listOf(
                    "/review",
                    "/pending-review"
                ).forEach { authorize(it, hasAnyRole(REVIEWER, EDITOR, SECTION_EDITOR, EIC, ADMIN)) }
                listOf(
                    "/submit",
                    "/profile/{profileId}",
                    "/profile/{profileId}/edit"
                ).forEach { authorize(it, hasAnyRole(AUTHOR, CORRESPONDING_AUTHOR, REVIEWER, EDITOR, SECTION_EDITOR, EIC, ADMIN)) }
                /*
                listOf(
                    "/", "/util.css", "/htmx.min.js", "/header", "/favicon.ico",
                    "/publication/{publicationId}",
                    "/publication/{publicationId}/section/{sectionId}",
                    "/publication/{publicationId}/section/{sectionId}/manuscript/{manuscriptId}",
                    "/archive",
                    "/archive/publication/{publicationId}",
                    "/archive/publication/{publicationId}/section/{sectionId}",
                    "/archive/publication/{publicationId}/section/{sectionId}/manuscript/{manuscriptId}",
                    "/login.html",
                    "/register.html",
                    "/contact",
                    "/coming-soon",
                    "/login"
                ).forEach { authorize(it, permitAll) }
                */
                authorize(anyRequest, permitAll)
            }
        }
        return http.build()
    }
}