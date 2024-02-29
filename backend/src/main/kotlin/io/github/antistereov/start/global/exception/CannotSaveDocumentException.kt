package io.github.antistereov.start.global.exception

class CannotSaveDocumentException(id: Any, clazz: Class<*>, cause: Throwable):
    RuntimeException("Cannot save document of type ${clazz.simpleName} with id $id: ${cause.message}")