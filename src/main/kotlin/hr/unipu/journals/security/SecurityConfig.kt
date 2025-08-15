package hr.unipu.journals.security

import hr.unipu.journals.feature.account.AccountRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {
    @Bean
    fun root(): UserDetailsService = InMemoryUserDetailsManager(User
        .withUsername("@root")
        .password("d49764c083bc63ec091ac23711316de034ab62d188e02e1f2c11455c2b99d8d8")
        .roles(ROOT)
        .build()
    )
    @Bean
    fun securityFilterChain(http: HttpSecurity, accountRepository: AccountRepository): SecurityFilterChain = http
        .csrf { it.disable() } // Required to use the H2 console, comment out in production
        .headers { it.frameOptions { frame -> frame.disable() } }
        .formLogin {
            it.loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
        }
        .logout { it
            .logoutUrl("/logout")
            .logoutSuccessUrl("/")
            .deleteCookies("JSESSIONID")
            .invalidateHttpSession(true)
        }
        .sessionManagement { it
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .invalidSessionUrl("/login?expired")
            //.maximumSessions(1)
            //.expiredUrl("/login?expired")
        }
        .userDetailsService { email ->
            if(!accountRepository.emailExists(email)) throw UsernameNotFoundException("$email not found")
            val account = accountRepository.byEmail(email)
            SecurityUser(account, if(account.isAdmin) listOf(SimpleGrantedAuthority(ADMIN)) else emptyList())
        }
        .authorizeHttpRequests { it
            .requestMatchers("h2-console/**").permitAll() // comment out in production
            .requestMatchers("/root").hasRole(ROOT)
            .requestMatchers(
                "/publication/{publicationId}/configure-eic-on-publication",
                "/hidden",
                "/hidden/{publicationId}",
                "/hidden/{publicationId}/{sectionId}",
                "/hidden/{publicationId}/{sectionId}/{manuscriptId}",
            ).hasRole(ADMIN)
            .requestMatchers(
                "/eic-initial-review",
                "/technical-processing-page"
            ).hasAnyRole(EIC, ADMIN)
            .requestMatchers(
                "/review-round-initialization",
                "/manage-manuscript-under-review",
            ).hasAnyRole(EDITOR, SECTION_EDITOR, EIC, ADMIN)
            .requestMatchers(
                "/review",
                "/pending-review"
            ).hasAnyRole(REVIEWER, EDITOR, SECTION_EDITOR, EIC, ADMIN)
            .requestMatchers(
                "/submit",
                "/profile/{profileId}",
                "/profile/{profileId}/edit"
            )
            .hasAnyRole(AUTHOR, CORRESPONDING_AUTHOR, REVIEWER,EDITOR,SECTION_EDITOR, EIC, ADMIN)
            .requestMatchers(
                "/", "/util.css", "/htmx.min.js", "/font.min.js", "/header",
                "/publication", // publication-page
                "/publication/{publicationId}", // section-page
                "/publication/{publicationId}/{sectionId}", // manuscript-page
                "/publication/{publicationId}/{sectionId}/{manuscriptId}", // manuscript-detail
                "/archive",
                "/archive/{publicationId}",
                "/archive/{publicationId}/{sectionId}",
                "/archive/{publicationId}/{sectionId}/{manuscriptId}",
                "/login",
                "/register",
                "/contact",
                "/coming-soon"
            ).permitAll()
            .anyRequest().authenticated()
        }.build()
}