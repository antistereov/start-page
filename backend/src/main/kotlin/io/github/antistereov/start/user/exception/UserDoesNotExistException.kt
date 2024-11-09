package io.github.antistereov.start.user.exception

class UserDoesNotExistException(
    val userId: String
) : UserServiceException(
    message = "User $userId does not exists"
)