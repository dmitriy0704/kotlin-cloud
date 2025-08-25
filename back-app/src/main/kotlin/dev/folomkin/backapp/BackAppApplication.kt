package dev.folomkin.backapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BackAppApplication

fun main(args: Array<String>) {
    runApplication<BackAppApplication>(*args)
}
