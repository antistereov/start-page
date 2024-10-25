package io.github.antistereov.start.global.controller

import io.github.antistereov.start.auth.exception.AuthServiceException
import io.github.antistereov.start.auth.exception.InvalidCredentialsException
import io.github.antistereov.start.auth.exception.MissingClaimException
import io.github.antistereov.start.global.exception.*
import io.github.antistereov.start.user.exception.UserServiceException
import io.github.antistereov.start.user.exception.UsernameAlreadyExistsException
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
        // AuthService
        InvalidCredentialsException::class.java to HttpStatus.UNAUTHORIZED,
        AuthServiceException::class.java to HttpStatus.INTERNAL_SERVER_ERROR,

        // UserService
        UsernameAlreadyExistsException::class.java to HttpStatus.BAD_REQUEST,
        UserServiceException::class.java to HttpStatus.INTERNAL_SERVER_ERROR,

        // Custom exceptions
        CannotSaveDocumentException::class.java to HttpStatus.BAD_REQUEST,
        CannotDeleteDocumentException::class.java to HttpStatus.BAD_REQUEST,
        DocumentExistsException::class.java to HttpStatus.BAD_REQUEST,
        DocumentNotFoundException::class.java to HttpStatus.NOT_FOUND,
        ExpiredTokenException::class.java to HttpStatus.BAD_REQUEST,
        InvalidCallbackException::class.java to HttpStatus.BAD_REQUEST,
        InvalidNextcloudCredentialsException::class.java to HttpStatus.UNAUTHORIZED,
        InvalidPrincipalException::class.java to HttpStatus.UNAUTHORIZED,
        InvalidStateParameterException::class.java to HttpStatus.BAD_REQUEST,
        InvalidThirdPartyAPIResponseException::class.java to HttpStatus.BAD_REQUEST,
        MessageLimitExceededException::class.java to HttpStatus.BAD_REQUEST,
        MissingClaimException::class.java to HttpStatus.BAD_REQUEST,
        MissingCredentialsException::class.java to HttpStatus.BAD_REQUEST,
        NetworkErrorException::class.java to HttpStatus.INTERNAL_SERVER_ERROR,
        ParsingErrorException::class.java to HttpStatus.BAD_REQUEST,
        ResourceReadOnlyException::class.java to HttpStatus.FORBIDDEN,
        ServiceException::class.java to HttpStatus.INTERNAL_SERVER_ERROR,
        ThirdPartyAPIException::class.java to HttpStatus.INTERNAL_SERVER_ERROR,
        ThirdPartyAuthorizationCanceledException::class.java to HttpStatus.UNAUTHORIZED,
        TimeoutException::class.java to HttpStatus.REQUEST_TIMEOUT,
        UnexpectedErrorException::class.java to HttpStatus.INTERNAL_SERVER_ERROR,
        JwtDecoderInitializationException::class.java to HttpStatus.INTERNAL_SERVER_ERROR,

        // Spring exceptions
        IndexOutOfBoundsException::class.java to HttpStatus.BAD_REQUEST,
        IllegalArgumentException::class.java to HttpStatus.BAD_REQUEST,
        TimeoutException::class.java to HttpStatus.REQUEST_TIMEOUT,
        DnsNameResolverException::class.java to HttpStatus.REQUEST_TIMEOUT,
        SslHandshakeTimeoutException::class.java to HttpStatus.REQUEST_TIMEOUT,

        // ical4j exceptions
        NetworkErrorException::class.java to HttpStatus.INTERNAL_SERVER_ERROR,
        ParsingErrorException::class.java to HttpStatus.BAD_REQUEST,
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
