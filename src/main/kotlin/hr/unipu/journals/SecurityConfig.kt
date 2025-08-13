package hr.unipu.journals

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
            .authorizeHttpRequests { // "/h2-console/**"
                it.requestMatchers("/**").permitAll()
                    .anyRequest().authenticated()
            }
            .csrf { it.disable() } // Required to use the H2 console
            .headers { it.frameOptions { frame -> frame.disable() } } // Allow use of frames

        return http.build()
    }
}