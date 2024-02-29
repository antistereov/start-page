package io.github.antistereov.start.global.exception

class CannotDeleteDocumentException(id: Any, clazz: Class<*>, cause: Throwable):
    RuntimeException("Cannot delete document of type ${clazz.simpleName} with id $id: ${cause.message}")