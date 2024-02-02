package io.github.antistereov.start.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@Configuration
class PasswordEncoderConfig {
    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()
}