package io.github.antistereov.start.model

class UserNotFoundException(userId: String) : RuntimeException("User not found: $userId")