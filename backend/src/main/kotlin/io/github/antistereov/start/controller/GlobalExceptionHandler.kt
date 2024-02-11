package io.github.antistereov.start.controller

import io.github.antistereov.start.model.InvalidPrincipalException
import io.github.antistereov.start.model.MissingClaimException
import io.github.antistereov.start.model.ServiceException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import reactor.core.publisher.Mono

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleInternalServerError(ex: ServiceException): Mono<Map<String, Any>> {
        val message = ex.message ?: "Internal Server Error"
        return Mono.just(mapOf("error" to message))
    }

    @ExceptionHandler(InvalidPrincipalException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleInvalidPrincipalException(ex: InvalidPrincipalException): Mono<Map<String, Any>> {
        val message = ex.message ?: "Invalid Principal"
        return Mono.just(mapOf("error" to message))
    }

    @ExceptionHandler(MissingClaimException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMissingClaimException(ex: MissingClaimException): Mono<Map<String, Any>> {
        val message = ex.message ?: "Missing claim"
        return Mono.just(mapOf("error" to message))
    }
}