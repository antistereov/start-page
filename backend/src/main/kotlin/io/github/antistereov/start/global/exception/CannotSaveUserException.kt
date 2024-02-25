package io.github.antistereov.start.global.exception

class CannotSaveUserException(cause: Throwable?): RuntimeException("Failed to save user: $cause", cause)