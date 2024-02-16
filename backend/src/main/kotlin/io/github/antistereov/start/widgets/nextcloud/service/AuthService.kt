package io.github.antistereov.start.widgets.nextcloud.service

import io.github.antistereov.start.global.model.exception.CannotSaveUserException
import io.github.antistereov.start.global.model.exception.InvalidNextcloudCredentialsException
import io.github.antistereov.start.global.model.exception.MissingCredentialsException
import io.github.antistereov.start.global.model.exception.UserNotFoundException
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.util.UrlHandler
import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCredentials
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val aesEncryption: AESEncryption,
    private val urlHandler: UrlHandler,
    private val webClientBuilder: WebClient.Builder
) {

    private val serviceName = "Nextcloud"

    fun getCredentials(userId: String): Mono<NextcloudCredentials> {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .handle { user, sink ->
                val host = user.nextcloudHost
                val username = user.nextcloudUsername
                val password = user.nextcloudPassword

                if (host == null) {
                    sink.error(MissingCredentialsException(serviceName, "host URL", userId))
                    return@handle
                }

                if (username == null) {
                    sink.error(MissingCredentialsException(serviceName, "username", userId))
                    return@handle
                }

                if (password == null) {
                    sink.error(MissingCredentialsException(serviceName, "password", userId))
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
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .map { user ->
                user.nextcloudHost = aesEncryption.encrypt(urlHandler.normalizeBaseUrl(credentials.url))
                user.nextcloudUsername = aesEncryption.encrypt(credentials.username)
                user.nextcloudPassword = aesEncryption.encrypt(credentials.password)

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
        val webClient = webClientBuilder
            .baseUrl("${credentials.url}/remote.php/dav/files/${credentials.username}")
            .defaultHeaders { headers ->
                headers.setBasicAuth(credentials.username, credentials.password)
            }
            .build()

        return webClient.get()
            .retrieve()
            .onStatus({ it != HttpStatus.OK }, { Mono.just(InvalidNextcloudCredentialsException()) })
            .bodyToMono(String::class.java)
            .flatMap { Mono.just("Nextcloud credentials are valid.") }
    }
}