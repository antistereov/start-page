package io.github.antistereov.orbitab.user.exception

class UserDoesNotExistException(
    val userId: String
) : UserException(
    message = "User $userId does not exists"
)