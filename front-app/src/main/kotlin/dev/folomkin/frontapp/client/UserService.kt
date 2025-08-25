package dev.folomkin.frontapp.client

import org.springframework.stereotype.Service

@Service
class UserService(
    private val remoteServiceClient: RemoteServiceClient
) {

    fun fetchUser(id: Long): UserDto {
        return remoteServiceClient.getUserById(id)
    }

    fun createNewUser(name: String, email: String): UserDto {
        val userDto = UserDto(name = name, email = email)
        return remoteServiceClient.createUser(userDto)
    }

    fun getAllUsers(): List<UserDto> {
        return remoteServiceClient.getAllUsers()
    }
}