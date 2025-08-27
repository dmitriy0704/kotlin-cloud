package dev.folomkin.frontapp.feign.exceptions

import feign.FeignException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class FeignExceptionHandler {

    @ExceptionHandler(FeignException::class)
    fun handleFeignException(ex: FeignException): ResponseEntity<String> {
        return when (ex.status()) {
            404 -> ResponseEntity("Resource not found", HttpStatus.NOT_FOUND)
            400 -> ResponseEntity("Bad request", HttpStatus.BAD_REQUEST)
            else -> ResponseEntity(
                "Internal server error",
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        }
    }
}