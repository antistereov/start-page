package io.github.antistereov.start.util

import jakarta.persistence.Column
import org.springframework.stereotype.Component
import java.net.URL

@Component
class UrlHandler {

    fun normalizeBaseUrl(baseUrl: String): String {
        // Überprüfen, ob die URL gültig ist.
        val url = try {
            URL(baseUrl)
        } catch (e: Exception) {
            throw IllegalArgumentException("Provided string is not a valid URL")
        }

        // Überprüfen, ob die URL mit "/" endet und diesen entfernen.
        return if (url.toString().endsWith("/")) {
            url.toString().substring(0, url.toString().length - 1)
        } else {
            url.toString()
        }
    }
}