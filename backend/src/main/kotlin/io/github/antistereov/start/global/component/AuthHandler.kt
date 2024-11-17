package io.github.antistereov.start.global.component

import org.springframework.stereotype.Component
import java.util.*

@Component
class AuthHandler {
    fun createBasicAuthHeader(username: String, password: String): String {
        val credentials = "$username:$password"
        val encodedCredentials = Base64.getEncoder().encodeToString(credentials.toByteArray())
        return "Basic $encodedCredentials"
    }
}