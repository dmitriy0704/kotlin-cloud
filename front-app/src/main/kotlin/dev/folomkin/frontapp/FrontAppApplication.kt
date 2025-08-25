package dev.folomkin.frontapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.FeignClient

@SpringBootApplication
@EnableFeignClients
class FrontAppApplication

fun main(args: Array<String>) {
    runApplication<FrontAppApplication>(*args)
}
