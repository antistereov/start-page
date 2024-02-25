package io.github.antistereov.start.global.exception

class ResourceReadOnlyException(resourceName: String) : RuntimeException("Resource $resourceName is read-only.")