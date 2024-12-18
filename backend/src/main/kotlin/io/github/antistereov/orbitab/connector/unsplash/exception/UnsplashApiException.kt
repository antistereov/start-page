package io.github.antistereov.orbitab.connector.unsplash.exception

import org.springframework.http.HttpStatusCode

class UnsplashApiException(
    val uri: String,
    val statusCode: HttpStatusCode,
    message: String
) : UnsplashException(message)
