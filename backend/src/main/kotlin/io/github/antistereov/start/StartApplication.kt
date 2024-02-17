package io.github.antistereov.start

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class StartApplication

fun main(args: Array<String>) {
    runApplication<StartApplication>(*args)
}
