package io.github.antistereov.start.global.exception

class DocumentExistsException(clazz: Class<*>, message: String):
    RuntimeException("Document of type ${clazz.simpleName} already exists: $message")