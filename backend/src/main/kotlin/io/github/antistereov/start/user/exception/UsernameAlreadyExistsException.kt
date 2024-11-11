package io.github.antistereov.start.user.exception

class UsernameAlreadyExistsException(info: String) : UserException(
    message = "$info: Username already exists"
)