package io.github.antistereov.start.global.exception

import org.springframework.http.HttpStatusCode
import java.net.URI

class ThirdPartyAPIException(
    uri: URI,
    httpStatusCode: HttpStatusCode,
    message: String
) : RuntimeException(
    "Third-party API request to $uri failed with status code $httpStatusCode: $message"
)