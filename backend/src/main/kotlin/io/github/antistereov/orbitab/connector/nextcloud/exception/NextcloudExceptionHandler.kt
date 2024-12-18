package io.github.antistereov.orbitab.connector.nextcloud.exception

import io.github.antistereov.orbitab.global.model.ErrorResponse
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

        val statusCode = HttpStatus.INTERNAL_SERVER_ERROR

        val errorResponse = ErrorResponse(
            status = statusCode.value(),
            error = ex.javaClass.simpleName,
            message = "An error in the Nextcloud connector occurred: ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, statusCode)
    }

    @ExceptionHandler(NextcloudCredentialsException::class)
    suspend fun handleNextcloudCredentialsException(ex: NextcloudCredentialsException,
                                             exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "${ex.javaClass.simpleName} - ${ex.message}"}

        val statusCode = HttpStatus.FORBIDDEN

        val errorResponse = ErrorResponse(
            status = statusCode.value(),
            error = ex.javaClass.simpleName,
            message = "An exception occurred when getting saved Nextcloud user credentials: ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, statusCode)
    }

    @ExceptionHandler(NextcloudInvalidCredentialsException::class)
    suspend fun handleNextcloudInvalidCredentialsException(ex: NextcloudInvalidCredentialsException,
                                                    exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "${ex.javaClass.simpleName} - ${ex.message}"}

        val statusCode = HttpStatus.UNAUTHORIZED

        val errorResponse = ErrorResponse(
            status = statusCode.value(),
            error = ex.javaClass.simpleName,
            message = "Invalid Nextcloud user credentials: ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, statusCode)
    }
}