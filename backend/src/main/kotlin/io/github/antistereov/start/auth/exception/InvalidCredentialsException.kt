package io.github.antistereov.start.auth.exception

class InvalidCredentialsException : AuthException(
    message = "Login failed: Invalid credentials",
)