package dev.folomkin.kotlincloud

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KotlinCloudApplication

fun main(args: Array<String>) {
    runApplication<KotlinCloudApplication>(*args)
}
