package io.github.antistereov.orbitab

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OrbitabBackend

fun main(args: Array<String>) {
    runApplication<OrbitabBackend>(*args)
}
