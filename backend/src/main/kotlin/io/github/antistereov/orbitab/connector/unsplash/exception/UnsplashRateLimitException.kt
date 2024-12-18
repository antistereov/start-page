package io.github.antistereov.orbitab.connector.unsplash.exception

import org.springframework.http.HttpStatus

class UnsplashRateLimitException(
    val uri: String,
    val statusCode: HttpStatus,
    message: String
) : UnsplashException(message)