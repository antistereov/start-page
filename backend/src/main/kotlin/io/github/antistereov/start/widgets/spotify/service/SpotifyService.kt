package io.github.antistereov.start.widgets.spotify.service

import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.widgets.spotify.model.SpotifyTokenResponse
import io.github.antistereov.start.user.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
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

    fun authenticate(code: String, encryptedUserId: String): SpotifyTokenResponse {
        val response = webClient.post()
            .uri("https://accounts.spotify.com/api/token")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(
                BodyInserters.fromFormData("grant_type", "authorization_code")
                    .with("code", code)
                    .with("redirect_uri", redirectUri)
                    .with("client_id", clientId)
                    .with("client_secret", clientSecret)
            )
            .retrieve()
            .bodyToMono(SpotifyTokenResponse::class.java)
            .block() ?: throw RuntimeException("Request to Spotify API failed or timed out.")

        response.let {
            val userId = aesEncryption.decrypt(encryptedUserId)
            val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found: $userId") }

            val spotifyAccessToken = aesEncryption.encrypt(it.accessToken)
            val spotifyRefreshToken = aesEncryption.encrypt(it.refreshToken
                ?: throw RuntimeException("No refresh token found for user ${user.id}"))

            user.spotifyAccessToken = spotifyAccessToken
            user.spotifyRefreshToken = spotifyRefreshToken
            user.spotifyAccessTokenExpirationDate = LocalDateTime.now().plusSeconds(response.expiresIn)

            userRepository.save(user)

            return it
        }
    }

    fun refreshToken(userId: String): SpotifyTokenResponse {
        val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found: $userId") }

        val encryptedSpotifyRefreshToken = user.spotifyRefreshToken
            ?: throw RuntimeException("No refresh token found for user ${user.id}")

        val spotifyRefreshToken = aesEncryption.decrypt(encryptedSpotifyRefreshToken)

        val response = webClient.post()
            .uri("https://accounts.spotify.com/api/token")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(
                BodyInserters.fromFormData("grant_type", "refresh_token")
                    .with("refresh_token", spotifyRefreshToken)
                    .with("client_id", clientId)
                    .with("client_secret", clientSecret)
            )
            .retrieve()
            .bodyToMono(SpotifyTokenResponse::class.java)
            .block() ?: throw RuntimeException("Request to Spotify API failed or timed out.")

        user.spotifyAccessToken = aesEncryption.encrypt(response.accessToken)
        if (response.refreshToken != null) { user.spotifyRefreshToken = aesEncryption.encrypt(response.refreshToken) }
        user.spotifyAccessTokenExpirationDate = LocalDateTime.now().plusSeconds(response.expiresIn)

        userRepository.save(user)

        return response
    }

    fun getAccessToken(userId: String): String {
        val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found: $userId") }
        val currentTime = LocalDateTime.now()

        if (currentTime.isAfter(user.spotifyAccessTokenExpirationDate)) {
            val newTokens = this.refreshToken(userId)
            return newTokens.accessToken
        } else {
            val encryptedSpotifyAccessToken = user.spotifyAccessToken
                ?: throw RuntimeException("No refresh token found for user ${user.id}")

            return aesEncryption.decrypt(encryptedSpotifyAccessToken)
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