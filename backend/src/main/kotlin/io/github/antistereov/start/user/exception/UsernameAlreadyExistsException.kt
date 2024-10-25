package io.github.antistereov.start.user.exception

class UsernameAlreadyExistsException(val info: String) : UserServiceException(
    message = "$info: Username already exists"
)