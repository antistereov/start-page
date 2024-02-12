package io.github.antistereov.start.global.model

class CannotSaveUserException(cause: Throwable?): RuntimeException("Failed to save user.", cause)