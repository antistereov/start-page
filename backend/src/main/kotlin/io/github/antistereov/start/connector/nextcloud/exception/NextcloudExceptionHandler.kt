package io.github.antistereov.start.connector.nextcloud.exception

import io.github.antistereov.start.global.model.ErrorResponse
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ServerWebExchange

@ControllerAdvice
class NextcloudExceptionHandler {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @ExceptionHandler(NextcloudException::class)
    suspend fun handleNextcloudException(ex: NextcloudException, exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "${ex.javaClass.simpleName} - ${ex.message}" }

        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = ex.javaClass.simpleName,
            message = "An error in the Nextcloud connector occurred: ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(NextcloudCredentialsException::class)
    suspend fun handleNextcloudCredentialsException(ex: NextcloudCredentialsException,
                                             exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "${ex.javaClass.simpleName} - ${ex.message}"}

        val errorResponse = ErrorResponse(
            status = HttpStatus.FORBIDDEN.value(),
            error = ex.javaClass.simpleName,
            message = "An exception occurred when getting saved Nextcloud user credentials: ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(NextcloudInvalidCredentialsException::class)
    suspend fun handleNextcloudInvalidCredentialsException(ex: NextcloudInvalidCredentialsException,
                                                    exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "${ex.javaClass.simpleName} - ${ex.message}"}

        val errorResponse = ErrorResponse(
            status = HttpStatus.UNAUTHORIZED.value(),
            error = ex.javaClass.simpleName,
            message = "Invalid Nextcloud user credentials: ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED)
    }
}