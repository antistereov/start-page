package io.github.antistereov.start.widgets.instagram.service

import org.springframework.stereotype.Service

@Service
class InstagramService(
    private val instagramTokenService: InstagramTokenService,
) {

    private val serviceName = "Instagram"
}