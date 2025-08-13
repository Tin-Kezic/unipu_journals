package hr.unipu.journals.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() } // Required to use the H2 console, comment out in production
            .headers { it.frameOptions { frame -> frame.disable() } }
            .authorizeHttpRequests { it
                .requestMatchers("h2-console/**").permitAll() // comment out in production
                .requestMatchers("/root").hasRole("ROOT")
                .requestMatchers(
                    "/publication/{publicationId}/configure-eic-on-publication",
                ).hasRole("ADMIN")
                .requestMatchers(
                    "/", "/util.css", "/htmx.min.js",
                    "/publication", // publication-page
                    "/publication/{publicationId}", // section-page
                    "/publication/{publicationId}/{sectionId}", // manuscript-page
                    "/publication/{publicationId}/{sectionId}/{manuscriptId}", // manuscript-detail
                    "/archive",
                    "/archive/{publicationId}",
                    "/archive/{publicationId}/{sectionId}", //
                    "/archive/{publicationId}/{sectionId}/{manuscriptId}", // manuscript-details-page
                    "/login",
                    "/register",
                ).permitAll()
                .anyRequest().authenticated()
            }

        return http.build()
    }
}