package hr.unipu.journals.util

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app")
class AppProperties {
    lateinit var baseUrl: String
    lateinit var clamavPath: String
    lateinit var fileStoragePath: String
}
