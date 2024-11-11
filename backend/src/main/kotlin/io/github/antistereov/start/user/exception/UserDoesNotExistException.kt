package io.github.antistereov.start.user.exception

class UserDoesNotExistException(
    val userId: String
) : UserException(
    message = "User $userId does not exists"
)