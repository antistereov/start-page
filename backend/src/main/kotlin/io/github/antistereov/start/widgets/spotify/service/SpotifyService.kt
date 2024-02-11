package io.github.antistereov.start.widgets.spotify.service

import io.github.antistereov.start.model.CannotSaveUserException
import io.github.antistereov.start.model.NoRefreshTokenException
import io.github.antistereov.start.model.UserNotFoundException
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.widgets.spotify.model.SpotifyTokenResponse
import io.github.antistereov.start.user.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class SpotifyService(
    private val webClientBuilder: WebClient.Builder,
    private val webClient: WebClient,
    private val userRepository: UserRepository,
    private val aesEncryption: AESEncryption,
) {

    @Value("\${spotify.clientId}")
    private lateinit var clientId: String

    @Value("\${spotify.clientSecret}")
    private lateinit var clientSecret: String

    @Value("\${spotify.redirectUri}")
    private lateinit var redirectUri: String

    private val spotifyApiUrl = "https://api.spotify.com/v1"
    private val scopes = "user-read-currently-playing"

    fun getAuthorizationUrl(userId: String): String {
        val encryptedUserId = aesEncryption.encrypt(userId)

        return UriComponentsBuilder.fromHttpUrl("https://accounts.spotify.com/authorize")
            .queryParam("response_type", "code")
            .queryParam("client_id", clientId)
            .queryParam("scope", scopes)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("state", encryptedUserId)
            .toUriString()
    }

    fun authenticate(code: String, encryptedUserId: String): Mono<SpotifyTokenResponse> {
        return webClient
            .post()
            .uri("https://accounts.spotify.com/api/token")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                .with("code", code)
                .with("redirect_uri", redirectUri)
                .with("client_id", clientId)
                .with("client_secret", clientSecret)
            )
            .retrieve()
            .bodyToMono(SpotifyTokenResponse::class.java)
            .flatMap { response ->
                val userId = aesEncryption.decrypt(encryptedUserId)
                handleUser(userId, response)
            }
    }

    private fun handleUser(userId: String, response: SpotifyTokenResponse): Mono<SpotifyTokenResponse> {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                val encryptedRefreshToken = user.spotifyRefreshToken
                    ?: return@flatMap Mono.error(NoRefreshTokenException("Spotify", userId))
                val refreshToken = aesEncryption.decrypt(encryptedRefreshToken)
                val expirationDate = LocalDateTime.now().plusSeconds(response.expiresIn)

                user.spotifyAccessToken = aesEncryption.encrypt(response.accessToken)
                user.spotifyRefreshToken = refreshToken
                user.spotifyAccessTokenExpirationDate = expirationDate

                userRepository.save(user)
                    .onErrorMap { throwable ->
                        CannotSaveUserException(throwable)
                    }
                    .thenReturn(response)
            }
    }

    fun refreshToken(userId: String): Mono<SpotifyTokenResponse> {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                val encryptedRefreshToken = user.spotifyRefreshToken
                    ?: return@flatMap Mono.error(NoRefreshTokenException("Spotify", userId))
                val refreshToken = aesEncryption.decrypt(encryptedRefreshToken)

                webClient.post()
                    .uri("https://accounts.spotify.com/api/token")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(
                        BodyInserters.fromFormData("grant_type", "refresh_token")
                            .with("refresh_token", refreshToken)
                            .with("client_id", clientId)
                            .with("client_secret", clientSecret)
                    )
                    .retrieve()
                    .bodyToMono(SpotifyTokenResponse::class.java)
                    .flatMap { response ->
                        user.spotifyAccessToken = aesEncryption.encrypt(response.accessToken)
                        user.spotifyAccessTokenExpirationDate = LocalDateTime.now().plusSeconds(response.expiresIn)

                        userRepository.save(user)
                            .onErrorMap { throwable ->
                                CannotSaveUserException(throwable)
                            }
                            .thenReturn(response)
                    }
            }
    }

    fun getAccessToken(userId: String): Mono<String> {
        val currentTime = LocalDateTime.now()

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                if (currentTime.isAfter(user.spotifyAccessTokenExpirationDate)) {
                    this.refreshToken(userId).map { it.accessToken }
                } else {
                    val encryptedSpotifyAccessToken = user.spotifyAccessToken
                        ?: return@flatMap Mono.error(NoRefreshTokenException("Spotify", userId))
                    Mono.just(aesEncryption.decrypt(encryptedSpotifyAccessToken))
                }
            }
    }
    fun getCurrentSong(accessToken: String): Mono<String> {
        return webClientBuilder.build().get()
            .uri("$spotifyApiUrl/me/player/currently-playing")
            .headers { it.setBearerAuth(accessToken) }
            .retrieve()
            .onStatus({ status -> status.is4xxClientError || status.is5xxServerError }, {
                it.bodyToMono(String::class.java)
                    .flatMap { errorMessage ->
                        Mono.error(RuntimeException("Error from Spotify API: $errorMessage"))
                    }
            })
            .bodyToMono(String::class.java)
    }

}