package io.github.antistereov.orbitab.auth.exception

class InvalidCredentialsException : AuthException(
    message = "Login failed: Invalid credentials",
)