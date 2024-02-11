package io.github.antistereov.start.widgets.nextcloud.service

import io.github.antistereov.start.model.CannotSaveUserException
import io.github.antistereov.start.model.InvalidNextcloudCredentialsException
import io.github.antistereov.start.model.MissingNextcloudCredentialsException
import io.github.antistereov.start.model.UserNotFoundException
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

    fun getCredentials(userId: String): Mono<NextcloudCredentials> {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .handle { user, sink ->
                val host = user.nextcloudHost
                val username = user.nextcloudUsername
                val password = user.nextcloudPassword

                if (host == null) {
                    sink.error(MissingNextcloudCredentialsException(userId, "host URL"))
                    return@handle
                }

                if (username == null) {
                    sink.error(MissingNextcloudCredentialsException(userId, "username"))
                    return@handle
                }

                if (password == null) {
                    sink.error(MissingNextcloudCredentialsException(userId, "password"))
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
                user.nextcloudHost = urlHandler.normalizeBaseUrl(credentials.url)
                user.nextcloudUsername = credentials.username
                user.nextcloudPassword = credentials.password

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
            .baseUrl("${credentials.url}/ocs/v2.php/cloud/user")
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