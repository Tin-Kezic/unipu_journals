package hr.unipu.journals

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class CacheConfig {
    @Bean
    fun cacheManager(): CacheManager {
        val caffeine = Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .maximumSize(10_000)
        val pendingRegistrations = CaffeineCache("pendingRegistrations", caffeine.build())
        val pendingDeletions = CaffeineCache("pendingDeletions", caffeine.build())
        return SimpleCacheManager().apply { setCaches(listOf(pendingRegistrations, pendingDeletions)) }
    }
}