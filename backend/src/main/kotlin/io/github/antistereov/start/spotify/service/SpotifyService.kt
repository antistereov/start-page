package io.github.antistereov.start.spotify.service

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SpotifyService {

    @Value("\${spotify.clientId")
    private val clientId: String = ""

    @Value("\${spotify.clientSecret")
    private val clientSecret: String = ""

    @Value("\${spotify.redirectUri}")
    private val redirectUri: String = ""

}