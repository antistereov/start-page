package io.github.antistereov.start.global.exception

class DocumentNotFoundException(id: Any, clazz: Class<*>) :
    RuntimeException("Document of type ${clazz.simpleName} with id $id not found.")