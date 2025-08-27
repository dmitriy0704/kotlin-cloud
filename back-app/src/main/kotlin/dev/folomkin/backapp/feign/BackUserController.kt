package dev.folomkin.backapp.feign

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class DemoBackController {

    private val users = mutableListOf<UserDto>()

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): UserDto {
        return users.find { it.id == id }
            ?: throw RuntimeException("User not found")
    }

    @PostMapping
    fun createUser(@RequestBody userDto: UserDto): UserDto {
        val newUser = userDto.copy(id = (users.size + 1).toLong())
        users.add(newUser)
        return newUser
    }

    @GetMapping
    fun getAllUsers(): List<UserDto> {
        return users
    }
}

data class UserDto(
    val id: Long? = null,
    val name: String,
    val email: String
)