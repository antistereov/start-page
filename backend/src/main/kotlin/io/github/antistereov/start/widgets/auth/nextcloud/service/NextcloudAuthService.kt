package io.github.antistereov.start.widgets.auth.nextcloud.service

import io.github.antistereov.start.global.model.exception.CannotSaveUserException
import io.github.antistereov.start.global.model.exception.InvalidNextcloudCredentialsException
import io.github.antistereov.start.global.model.exception.MissingCredentialsException
import io.github.antistereov.start.global.model.exception.UserNotFoundException
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.widgets.auth.nextcloud.model.NextcloudCredentials
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.global.component.UrlHandler
import io.github.antistereov.start.widgets.auth.nextcloud.config.NextcloudProperties
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class NextcloudAuthService(
    private val userRepository: UserRepository,
    private val aesEncryption: AESEncryption,
    private val urlHandler: UrlHandler,
    private val webClientBuilder: WebClient.Builder,
    private val properties: NextcloudProperties,
) {

    private val logger = LoggerFactory.getLogger(NextcloudAuthService::class.java)

    fun getCredentials(userId: String): Mono<NextcloudCredentials> {
        logger.debug("Getting credentials for user: $userId.")

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .handle { user, sink ->
                val host = user.auth.nextcloud.host
                val username = user.auth.nextcloud.username
                val password = user.auth.nextcloud.password

                if (host == null) {
                    sink.error(MissingCredentialsException(properties.serviceName, "host URL", userId))
                    return@handle
                }

                if (username == null) {
                    sink.error(MissingCredentialsException(properties.serviceName, "username", userId))
                    return@handle
                }

                if (password == null) {
                    sink.error(MissingCredentialsException(properties.serviceName, "password", userId))
                    return@handle
                }

                sink.next(
                    NextcloudCredentials(
                        aesEncryption.decrypt(host),
                        aesEncryption.decrypt(username),
                        aesEncryption.decrypt(password)
                    )
                )
            }
    }

    fun authentication(userId: String, credentials: NextcloudCredentials): Mono<String> {
        logger.debug("Authenticating user: $userId.")

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .map { user ->
                user.auth.nextcloud.host = aesEncryption.encrypt(urlHandler.normalizeBaseUrl(credentials.host!!))
                user.auth.nextcloud.username = aesEncryption.encrypt(credentials.username!!)
                user.auth.nextcloud.password = aesEncryption.encrypt(credentials.password!!)

                user
            }
            .flatMap { user ->
                verifyCredentials(credentials)
                    .then(
                        userRepository.save(user)
                        .onErrorMap { throwable ->
                            CannotSaveUserException(throwable)
                        }
                    )
                    .map { "Credentials are correct, verified and have been saved." }
            }
    }

    fun verifyCredentials(credentials: NextcloudCredentials): Mono<String> {
        logger.debug("Verifying credentials.")

        val webClient = webClientBuilder
            .baseUrl("${credentials.host}/remote.php/dav/files/${credentials.username}")
            .defaultHeaders { headers ->
                headers.setBasicAuth(credentials.username!!, credentials.password!!)
            }
            .build()

        return webClient.get()
            .retrieve()
            .onStatus({ it != HttpStatus.OK }, { Mono.just(InvalidNextcloudCredentialsException()) })
            .bodyToMono(String::class.java)
            .flatMap { Mono.just("Nextcloud credentials are valid.") }
    }

    fun logout(userId: String): Mono<Void> {
        logger.debug("Logging out user: $userId.")

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                user.auth.nextcloud = NextcloudCredentials()

                userRepository.save(user)
                    .onErrorMap { throwable ->
                        CannotSaveUserException(throwable)
                    }
                    .then()
            }
    }
}