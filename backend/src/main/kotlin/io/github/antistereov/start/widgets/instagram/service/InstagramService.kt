package io.github.antistereov.start.widgets.instagram.service

import org.springframework.stereotype.Service

@Service
class InstagramService(
    private val instagramAuthService: InstagramAuthService,
) {

    private val serviceName = "Instagram"
}