package io.github.antistereov.start.global.controller

import io.github.antistereov.start.global.model.exception.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import reactor.core.publisher.Mono

@ControllerAdvice
class GlobalExceptionHandler {

    private val exceptionToHttpStatus = mapOf(
        CannotSaveUserException::class to HttpStatus.BAD_REQUEST,
        ExpiredTokenException::class to HttpStatus.BAD_REQUEST,
        InvalidCallbackException::class to HttpStatus.BAD_REQUEST,
        InvalidNextcloudCredentialsException::class to HttpStatus.UNAUTHORIZED,
        InvalidPrincipalException::class to HttpStatus.UNAUTHORIZED,
        InvalidStateParameterException::class to HttpStatus.BAD_REQUEST,
        InvalidThirdPartyAPIResponseException::class.java to HttpStatus.BAD_REQUEST,
        MissingClaimException::class to HttpStatus.BAD_REQUEST,
        MissingCredentialsException::class to HttpStatus.BAD_REQUEST,
        NoAccessTokenException::class to HttpStatus.BAD_REQUEST,
        NoRefreshTokenException::class to HttpStatus.BAD_REQUEST,
        ServiceException::class to HttpStatus.INTERNAL_SERVER_ERROR,
        ThirdPartyAPIException::class to HttpStatus.INTERNAL_SERVER_ERROR,
        ThirdPartyAuthorizationCanceledException::class to HttpStatus.UNAUTHORIZED,
        UnexpectedErrorException::class to HttpStatus.INTERNAL_SERVER_ERROR,
        UserNotFoundException::class to HttpStatus.NOT_FOUND
    )

    @ExceptionHandler
    fun handleException(ex: Exception): Mono<ResponseEntity<String>> {
        val status = exceptionToHttpStatus[ex::class] ?: HttpStatus.INTERNAL_SERVER_ERROR
        return createResponse(ex, status)
    }

    private fun createResponse(ex: Exception, status: HttpStatus): Mono<ResponseEntity<String>> {
        return Mono.just(ResponseEntity(ex.message, status))
    }
}