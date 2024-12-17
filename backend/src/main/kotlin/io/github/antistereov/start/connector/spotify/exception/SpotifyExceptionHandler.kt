package io.github.antistereov.start.connector.spotify.exception

import io.github.antistereov.start.global.model.ErrorResponse
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ServerWebExchange

@ControllerAdvice
class SpotifyExceptionHandler {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @ExceptionHandler(SpotifyException::class)
    suspend fun handleSpotifyException(ex: SpotifyException, exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "${ex.javaClass.simpleName} - ${ex.message}" }

        val statusCode = HttpStatus.INTERNAL_SERVER_ERROR

        val errorResponse = ErrorResponse(
            status = statusCode.value(),
            error = ex.javaClass.simpleName,
            message = "An error in the Spotify service occurred: ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, statusCode)
    }

    @ExceptionHandler(SpotifyTokenException::class)
    suspend fun handleSpotifyTokenException(ex: SpotifyTokenException,
                                     exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "${ex.javaClass.simpleName} - ${ex.message}"}

        val statusCode = HttpStatus.FORBIDDEN

        val errorResponse = ErrorResponse(
            status = statusCode.value(),
            error = ex.javaClass.simpleName,
            message = "An exception occurred when getting saved Spotify token information: ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, statusCode)
    }

    @ExceptionHandler(SpotifyInvalidCallbackException::class)
    suspend fun handleSpotifyInvalidCallbackException(ex: SpotifyInvalidCallbackException,
                                               exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "${ex.javaClass.simpleName} - ${ex.message}" }

        val statusCode = HttpStatus.BAD_REQUEST

        val errorResponse = ErrorResponse(
            status = statusCode.value(),
            error = ex.javaClass.simpleName,
            message = "An exception occurred after Spotify callback: ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, statusCode)
    }
}