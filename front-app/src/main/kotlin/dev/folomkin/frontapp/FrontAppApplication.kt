package dev.folomkin.frontapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FrontAppApplication

fun main(args: Array<String>) {
    runApplication<FrontAppApplication>(*args)
}
