package io.github.antistereov.orbitab.connector.unsplash.exception

import io.github.antistereov.orbitab.global.model.ErrorResponse
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ServerWebExchange

@ControllerAdvice
class UnsplashExceptionHandler {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @ExceptionHandler(UnsplashApiException::class)
    suspend fun handleUnsplashApiException(ex: UnsplashApiException,
                                           exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "${ex.javaClass.simpleName} - ${ex.message}"}

        val statusCode = ex.statusCode

        val errorResponse = ErrorResponse(
            status = statusCode.value(),
            error = ex.javaClass.simpleName,
            message = "An exception occurred when calling endpoint ${ex.uri}: ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, statusCode)
    }

    @ExceptionHandler(UnsplashException::class)
    suspend fun handleUnsplashException(ex: UnsplashException, exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "${ex.javaClass.simpleName} - ${ex.message}" }

        val statusCode = HttpStatus.INTERNAL_SERVER_ERROR

        val errorResponse = ErrorResponse(
            status = statusCode.value(),
            error = ex.javaClass.simpleName,
            message = "An error in the Unsplash service occurred: ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, statusCode)
    }

    @ExceptionHandler(UnsplashInvalidCallbackException::class)
    suspend fun handleUnsplashInvalidCallbackException(ex: UnsplashInvalidCallbackException,
                                                       exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "${ex.javaClass.simpleName} - ${ex.message}" }

        val statusCode = HttpStatus.BAD_REQUEST

        val errorResponse = ErrorResponse(
            status = statusCode.value(),
            error = ex.javaClass.simpleName,
            message = "An exception occurred after Unsplash callback: ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, statusCode)
    }

    @ExceptionHandler(UnsplashInvalidParameterException::class)
    suspend fun handleUnsplashInvalidParameterException(ex: UnsplashInvalidParameterException,
                                                        exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "${ex.javaClass.simpleName} - ${ex.message}" }

        val statusCode = HttpStatus.BAD_REQUEST

        val errorResponse = ErrorResponse(
            status = statusCode.value(),
            error = ex.javaClass.simpleName,
            message = "Illegal request parameter: ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, statusCode)
    }

    @ExceptionHandler(UnsplashRateLimitException::class)
    suspend fun handleUnsplashRateLimitException(ex: UnsplashRateLimitException,
                                                 exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        logger.warn(ex) { "${ex.javaClass.simpleName} - ${ex.message}"}

        val statusCode = HttpStatus.TOO_MANY_REQUESTS

        val errorResponse = ErrorResponse(
            status = statusCode.value(),
            error = ex.javaClass.simpleName,
            message = "Exceeded allowed API request limit: ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, statusCode)
    }

    @ExceptionHandler(UnsplashTokenException::class)
    suspend fun handleUnsplashTokenException(ex: UnsplashTokenException,
                                             exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "${ex.javaClass.simpleName} - ${ex.message}"}

        val statusCode = HttpStatus.FORBIDDEN

        val errorResponse = ErrorResponse(
            status = statusCode.value(),
            error = ex.javaClass.simpleName,
            message = "An exception occurred when getting saved Unsplash token information: ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, statusCode)
    }
}