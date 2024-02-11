package io.github.antistereov.start.model

class CannotSaveUserException(cause: Throwable?): RuntimeException("Failed to save user.", cause)