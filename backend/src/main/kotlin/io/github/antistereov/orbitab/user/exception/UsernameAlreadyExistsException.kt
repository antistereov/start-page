package io.github.antistereov.orbitab.user.exception

class UsernameAlreadyExistsException(info: String) : UserException(
    message = "$info: Username already exists"
)