package io.github.antistereov.start.auth.exception

class LoginFailedException : AuthServiceException(
    message = "Login failed",
)