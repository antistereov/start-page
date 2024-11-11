package io.github.antistereov.start.widget.spotify.exception

import io.github.antistereov.start.global.exception.ErrorResponse
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

        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = ex.javaClass.simpleName,
            message = "An error in the Spotify service occurred: ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(SpotifyTokenException::class)
    suspend fun handleSpotifyTokenException(ex: SpotifyTokenException,
                                     exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "${ex.javaClass.simpleName} - ${ex.message}"}

        val errorResponse = ErrorResponse(
            status = HttpStatus.FORBIDDEN.value(),
            error = ex.javaClass.simpleName,
            message = "An exception occurred when getting saved Spotify token information: ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(SpotifyInvalidCallbackException::class)
    suspend fun handleSpotifyInvalidCallbackException(ex: SpotifyInvalidCallbackException,
                                               exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "${ex.javaClass.simpleName} - ${ex.message}" }

        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = ex.javaClass.simpleName,
            message = "An exception occurred after Spotify callback: ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }
}