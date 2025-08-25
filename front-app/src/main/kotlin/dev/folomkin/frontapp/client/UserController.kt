package dev.folomkin.frontapp.client

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/client/users")
class UserController(
    val userService: UserService
) {


    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ResponseEntity<UserDto> {
        val user = userService.fetchUser(id)
        return ResponseEntity.ok(user)
    }

    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest): ResponseEntity<UserDto> {
        val user = userService.createNewUser(request.name, request.email)
        return ResponseEntity.ok(user)
    }

    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserDto>> {
        val users = userService.getAllUsers()
        return ResponseEntity.ok(users)
    }
}

data class CreateUserRequest(
    val name: String,
    val email: String
)