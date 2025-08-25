package dev.folomkin.frontapp.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody


@FeignClient(name = "remote-service", url = "\${remote.service.url}")

interface RemoteServiceClient {

    @GetMapping("/api/users/{id}")
    fun getUserById(@PathVariable id: Long): UserDto

    @PostMapping("/api/users")
    fun createUser(@RequestBody userDto: UserDto): UserDto

    @GetMapping("/api/users")
    fun getAllUsers(): List<UserDto>
}

data class UserDto(
    val id: Long? = null,
    val name: String,
    val email: String
)