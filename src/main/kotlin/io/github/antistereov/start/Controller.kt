package io.github.antistereov.start

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class MyController {

    @GetMapping("/private")
    @PreAuthorize("hasAuthority('role:admin')")
    fun privateEndpoint(): String {
        return "Welcome to a private endpoint!"
    }
}