package io.github.antistereov.start.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository

@Configuration
class SecurityContextConfig {

    @Bean
    fun securityContextRepository(): ServerSecurityContextRepository {
        return WebSessionServerSecurityContextRepository()
    }
}