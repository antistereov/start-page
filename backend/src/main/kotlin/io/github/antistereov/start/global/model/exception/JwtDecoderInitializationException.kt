package io.github.antistereov.start.global.model.exception

class JwtDecoderInitializationException(cause: Throwable) : RuntimeException("Could not initialize JwtDecoder: ${cause.message}", cause)