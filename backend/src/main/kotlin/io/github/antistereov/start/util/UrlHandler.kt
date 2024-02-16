package io.github.antistereov.start.util

import org.springframework.stereotype.Component
import java.net.URL

@Component
class UrlHandler {

    fun normalizeBaseUrl(baseUrl: String): String {
        val url = try {
            URL(baseUrl)
        } catch (e: Exception) {
            throw IllegalArgumentException("Provided string is not a valid URL")
        }

        return if (url.toString().endsWith("/")) {
            url.toString().substring(0, url.toString().length - 1)
        } else {
            url.toString()
        }
    }
}