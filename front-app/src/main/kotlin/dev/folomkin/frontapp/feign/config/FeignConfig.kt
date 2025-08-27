package dev.folomkin.frontapp.feign.config

import feign.Logger
import feign.Request
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FeignConfig {

    @Bean
    fun feignLoggerLevel(): Logger.Level {
        return Logger.Level.FULL
    }

    @Bean
    fun feignOptions(): Request.Options {
        return Request.Options(
            5000, // connectTimeout в миллисекундах
            10000 // readTimeout в миллисекундах
        )
    }
}