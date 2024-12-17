package io.github.antistereov.start.auth.exception

import io.github.antistereov.start.global.model.ErrorResponse
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ServerWebExchange

@ControllerAdvice
class AuthExceptionHandler {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @ExceptionHandler(AuthException::class)
    suspend fun handleAuthException(ex: AuthException, exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "${ex.javaClass.simpleName} - ${ex.message}" }

        val statusCode = HttpStatus.INTERNAL_SERVER_ERROR

        val errorResponse = ErrorResponse(
            status = statusCode.value(),
            error = ex.javaClass.simpleName,
            message = "A authentication error occurred: ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, statusCode)
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    suspend fun handleInvalidCredentialsException(ex: InvalidCredentialsException, exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "${ex.javaClass.simpleName} - ${ex.message}" }

        val statusCode = HttpStatus.UNAUTHORIZED

        val errorResponse = ErrorResponse(
            status = statusCode.value(),
            error = ex.javaClass.simpleName,
            message = "Invalid credentials: ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, statusCode)
    }

    @ExceptionHandler(InvalidPrincipalException::class)
    suspend fun handleInvalidPrincipalException(ex: InvalidPrincipalException, exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "${ex.javaClass.simpleName} - ${ex.message}" }

        val statusCode = HttpStatus.FORBIDDEN

        val errorResponse = ErrorResponse(
            status = statusCode.value(),
            error = ex.javaClass.simpleName,
            message = "Invalid principal: ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, statusCode)
    }

    @ExceptionHandler(AccessTokenExpiredException::class)
    suspend fun handleAccessTokenExpiredException(ex: AccessTokenExpiredException, exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "${ex.javaClass.simpleName} - ${ex.message}" }

        val statusCode = HttpStatus.UNAUTHORIZED

        val errorResponse = ErrorResponse(
            status = statusCode.value(),
            error = ex.javaClass.simpleName,
            message = "${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, statusCode)
    }
}