package io.github.antistereov.start.global.exception

class DocumentExistsException(id: Any, clazz: Class<*>):
    RuntimeException("Document of type ${clazz.simpleName} with id $id already exists.")