package io.github.antistereov.start.global.model.exception

class ResourceReadOnlyException(resourceName: String) : RuntimeException("Resource $resourceName is read-only.")