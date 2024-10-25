package io.github.antistereov.start.auth.exception

class InvalidCredentialsException : AuthServiceException(
    message = "Login failed: Invalid credentials",
)