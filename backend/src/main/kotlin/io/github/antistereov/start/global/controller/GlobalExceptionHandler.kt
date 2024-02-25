package io.github.antistereov.start.global.controller

import io.netty.handler.ssl.SslHandshakeTimeoutException
import io.netty.resolver.dns.DnsNameResolverException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    private val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    private val exceptionToHttpStatus = mapOf(
        // Custom exceptions
        io.github.antistereov.start.global.exception.CannotSaveUserException::class.java to HttpStatus.BAD_REQUEST,
        io.github.antistereov.start.global.exception.ExpiredTokenException::class.java to HttpStatus.BAD_REQUEST,
        io.github.antistereov.start.global.exception.InvalidCallbackException::class.java to HttpStatus.BAD_REQUEST,
        io.github.antistereov.start.global.exception.InvalidNextcloudCredentialsException::class.java to HttpStatus.UNAUTHORIZED,
        io.github.antistereov.start.global.exception.InvalidPrincipalException::class.java to HttpStatus.UNAUTHORIZED,
        io.github.antistereov.start.global.exception.InvalidStateParameterException::class.java to HttpStatus.BAD_REQUEST,
        io.github.antistereov.start.global.exception.InvalidThirdPartyAPIResponseException::class.java to HttpStatus.BAD_REQUEST,
        io.github.antistereov.start.global.exception.MessageLimitExceededException::class.java to HttpStatus.BAD_REQUEST,
        io.github.antistereov.start.global.exception.MissingClaimException::class.java to HttpStatus.BAD_REQUEST,
        io.github.antistereov.start.global.exception.MissingCredentialsException::class.java to HttpStatus.BAD_REQUEST,
        io.github.antistereov.start.global.exception.NetworkErrorException::class.java to HttpStatus.INTERNAL_SERVER_ERROR,
        io.github.antistereov.start.global.exception.ParsingErrorException::class.java to HttpStatus.BAD_REQUEST,
        io.github.antistereov.start.global.exception.ResourceReadOnlyException::class.java to HttpStatus.FORBIDDEN,
        io.github.antistereov.start.global.exception.ServiceException::class.java to HttpStatus.INTERNAL_SERVER_ERROR,
        io.github.antistereov.start.global.exception.ThirdPartyAPIException::class.java to HttpStatus.INTERNAL_SERVER_ERROR,
        io.github.antistereov.start.global.exception.ThirdPartyAuthorizationCanceledException::class.java to HttpStatus.UNAUTHORIZED,
        io.github.antistereov.start.global.exception.TimeoutException::class.java to HttpStatus.REQUEST_TIMEOUT,
        io.github.antistereov.start.global.exception.UnexpectedErrorException::class.java to HttpStatus.INTERNAL_SERVER_ERROR,
        io.github.antistereov.start.global.exception.UserNotFoundException::class.java to HttpStatus.NOT_FOUND,
        io.github.antistereov.start.global.exception.JwtDecoderInitializationException::class.java to HttpStatus.INTERNAL_SERVER_ERROR,

        // Spring exceptions
        IndexOutOfBoundsException::class.java to HttpStatus.BAD_REQUEST,
        IllegalArgumentException::class.java to HttpStatus.BAD_REQUEST,
        io.github.antistereov.start.global.exception.TimeoutException::class.java to HttpStatus.REQUEST_TIMEOUT,
        DnsNameResolverException::class.java to HttpStatus.REQUEST_TIMEOUT,
        SslHandshakeTimeoutException::class.java to HttpStatus.REQUEST_TIMEOUT,

        // ical4j exceptions
        io.github.antistereov.start.global.exception.NetworkErrorException::class.java to HttpStatus.INTERNAL_SERVER_ERROR,
        io.github.antistereov.start.global.exception.ParsingErrorException::class.java to HttpStatus.BAD_REQUEST,
    )

    @ExceptionHandler
    fun handleException(ex: Exception): ResponseEntity<Map<String, Any?>> {
        logger.error(ex.message)

        val status = exceptionToHttpStatus[ex::class.java]
            ?: HttpStatus.INTERNAL_SERVER_ERROR
        val errorAttributes = mapOf(
            "status" to status,
            "message" to (ex.message ?: "An error occurred"),
            "error" to ex::class.simpleName,
        )
        return ResponseEntity.status(status).body(errorAttributes)
    }
}
