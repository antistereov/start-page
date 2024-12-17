package io.github.antistereov.start.connector.unsplash.exception

import org.springframework.http.HttpStatusCode

class UnsplashApiException(
    val uri: String,
    val statusCode: HttpStatusCode,
    message: String
) : UnsplashException(message)
