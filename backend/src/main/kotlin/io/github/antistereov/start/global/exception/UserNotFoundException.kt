package io.github.antistereov.start.global.exception

class UserNotFoundException(userId: String) : RuntimeException("User not found: $userId")