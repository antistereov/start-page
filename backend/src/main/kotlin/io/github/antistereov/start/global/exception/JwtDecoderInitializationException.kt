package io.github.antistereov.start.global.exception

class JwtDecoderInitializationException(cause: Throwable) : RuntimeException("Could not initialize JwtDecoder: ${cause.message}", cause)