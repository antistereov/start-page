package io.github.antistereov.start.global.controller

import io.github.antistereov.start.global.model.*
import io.github.antistereov.start.model.*
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import reactor.core.publisher.Mono

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(CannotSaveUserException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleCannotSaveUser(ex: CannotSaveUserException): Mono<Map<String, Any>> {
        val message = ex.message ?: "Failed to save user"
        return Mono.just(mapOf("error" to message))
    }

    @ExceptionHandler(InvalidNextcloudCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handeInvalidNextcloudCredentials(ex: InvalidNextcloudCredentialsException): Mono<Map<String, Any>> {
        val message = ex.message ?: "Invalid Nextcloud credentials"
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

    @ExceptionHandler(MissingNextcloudCredentialsException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMissingNextcloudCredentials(ex: MissingNextcloudCredentialsException): Mono<Map<String, Any>> {
        val message = ex.message ?: "Missing Nextcloud credentials"
        return Mono.just(mapOf("error" to message))
    }

    @ExceptionHandler(NoAccessTokenException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMissingClaimException(ex: NoAccessTokenException): Mono<Map<String, Any>> {
        val message = ex.message ?: "No access token found"
        return Mono.just(mapOf("error" to message))
    }

    @ExceptionHandler(NoRefreshTokenException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMissingClaimException(ex: NoRefreshTokenException): Mono<Map<String, Any>> {
        val message = ex.message ?: "No refresh token found"
        return Mono.just(mapOf("error" to message))
    }

    @ExceptionHandler(ServiceException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleInternalServerError(ex: ServiceException): Mono<Map<String, Any>> {
        val message = ex.message ?: "Internal Server Error"
        return Mono.just(mapOf("error" to message))
    }

    @ExceptionHandler(SpotifyAPIException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleMissingClaimException(ex: SpotifyAPIException): Mono<Map<String, Any>> {
        val message = ex.message ?: "Error from Spotify API"
        return Mono.just(mapOf("error" to message))
    }

    @ExceptionHandler(UnexpectedErrorException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleInternalServerError(ex: UnexpectedErrorException): Mono<Map<String, Any>> {
        val message = ex.message ?: "Unexpected Error"
        return Mono.just(mapOf("error" to message))
    }

    @ExceptionHandler(UserNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleUserNotFound(ex: UserNotFoundException): Mono<Map<String, Any>> {
        val message = ex.message ?: "User not found"
        return Mono.just(mapOf("error" to message))
    }
}