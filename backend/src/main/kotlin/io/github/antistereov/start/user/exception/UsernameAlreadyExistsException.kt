package io.github.antistereov.start.user.exception

class UsernameAlreadyExistsException(info: String) : UserServiceException(
    message = "$info: Username already exists"
)