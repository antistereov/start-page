package io.github.antistereov.start.global.model.exception

class CannotSaveUserException(cause: Throwable?): RuntimeException("Failed to save user.", cause)