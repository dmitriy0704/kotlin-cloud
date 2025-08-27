# Общая теория

Я покажу пример реализации вызова методов REST-контроллера одного Spring Boot
приложения из другого с использованием OpenFeign на Kotlin.

1. Сначала добавим зависимости в `build.gradle.kts` вызывающего приложения:

```kotlin
dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign:4.1.3")
}
```

2. Создадим интерфейс Feign-клиента в вызывающем приложении:

```kotlin
package com.example.client

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
```

3. В файле `application.properties` укажем URL удаленного сервиса:

```properties
remote.service.url=http://localhost:8081
```

4. Включим поддержку Feign в главном классе приложения:

```kotlin
package com.example.client

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
class ClientApplication

fun main(args: Array<String>) {
    runApplication<ClientApplication>(*args)
}
```

5. Пример использования Feign-клиента в сервисе:

```kotlin
package com.example.client

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
```

6. Для примера, вот как может выглядеть контроллер в вызываемом приложении:

```kotlin
package com.example.server

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController {

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
```

7. Обработка ошибок (опционально):

```kotlin
package com.example.client

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
```

8. Конфигурация Feign (опционально, для настройки таймаутов и логирования):

```kotlin
package com.example.client

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
```

Основные моменты:

- `@FeignClient` определяет клиент для вызова удаленного сервиса
- URL сервиса можно вынести в конфигурацию
- Интерфейс клиента повторяет сигнатуры методов удаленного контроллера
- Аннотации Spring Web (@GetMapping, @PostMapping и т.д.) используются для
  описания HTTP-методов
- Можно настроить обработку ошибок, таймауты и логирование
- Feign автоматически преобразует ответы в объекты Kotlin (при правильной
  конфигурации)

Для работы примера:

1. Запустите серверное приложение (порт 8081)
2. Запустите клиентское приложение
3. Используйте UserService для вызова методов удаленного сервиса

Это базовый пример, который можно расширить добавлением:

- Аутентификации
- Retry-механизмов
- Circuit Breaker (например, с Resilience4j)
- Более сложной обработки ошибок
- Асинхронных вызовов

------
========

## **как использовать userservice для вызова методов удаленного сервиса?**

Для использования `UserService` из предыдущего примера для вызова методов
удаленного сервиса в Spring Boot приложении на Kotlin, нужно внедрить
`UserService` в компонент (например, контроллер или другой сервис) и вызвать его
методы. Ниже я покажу, как это сделать, с примерами для различных сценариев.

### Шаги по использованию `UserService`

1. **Убедитесь, что `UserService` правильно настроен**:
    - `UserService` уже определен в клиентском приложении и использует
      `RemoteServiceClient` (Feign-клиент) для взаимодействия с удаленным
      сервисом.
    - Убедитесь, что удаленный сервис (серверное приложение) запущен и доступен
      по URL, указанному в `application.properties` (например,
      `http://localhost:8081`).

2. **Внедрение `UserService` в контроллер**:
   Создайте REST-контроллер в клиентском приложении, который будет использовать
   `UserService` для вызова методов удаленного сервиса.

```kotlin
package com.example.client

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
    private val userService: UserService
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
```

3. **Объяснение кода**:
    - `UserService` внедряется в `UserController` через конструктор (Spring
      автоматически предоставит экземпляр благодаря аннотации `@Service` на
      `UserService`).
    - Методы контроллера вызывают соответствующие методы `UserService`:
        - `fetchUser(id)` — вызывает метод `getUserById` Feign-клиента для
          получения пользователя по ID.
        - `createNewUser(name, email)` — вызывает метод `createUser`
          Feign-клиента для создания нового пользователя.
        - `getAllUsers()` — вызывает метод `getAllUsers` Feign-клиента для
          получения списка всех пользователей.
    - `CreateUserRequest` — вспомогательный класс для обработки входных данных
      при создании пользователя.

4. **Пример вызова методов через HTTP-запросы**:
   После запуска клиентского приложения (например, на порту 8080), вы можете
   использовать инструменты вроде `curl` или Postman для отправки запросов:

    - **Получить пользователя по ID**:
      ```bash
      curl http://localhost:8080/api/client/users/1
      ```
      Ответ (если пользователь существует):
      ```json
      {"id":1,"name":"John Doe","email":"john@example.com"}
      ```

    - **Создать нового пользователя**:
      ```bash
      curl -X POST http://localhost:8080/api/client/users \
      -H "Content-Type: application/json" \
      -d '{"name":"Jane Doe","email":"jane@example.com"}'
      ```
      Ответ:
      ```json
      {"id":2,"name":"Jane Doe","email":"jane@example.com"}
      ```

    - **Получить всех пользователей**:
      ```bash
      curl http://localhost:8080/api/client/users
      ```
      Ответ:
      ```json
      [{"id":1,"name":"John Doe","email":"john@example.com"},{"id":2,"name":"Jane Doe","email":"jane@example.com"}]
      ```

5. **Использование `UserService` в другом сервисе**:
   Если вы хотите использовать `UserService` не в контроллере, а в другом
   сервисе, процесс аналогичен:

```kotlin
package com.example.client

import org.springframework.stereotype.Service

@Service
class AnotherService(
    private val userService: UserService
) {

    fun processUser(id: Long): String {
        val user = userService.fetchUser(id)
        return "Processed user: ${user.name}"
    }

    fun createAndProcessUser(name: String, email: String): String {
        val user = userService.createNewUser(name, email)
        return "Created user: ${user.name} with ID ${user.id}"
    }
}
```

6. **Обработка ошибок**:
   Если удаленный сервис вернет ошибку (например, 404 для несуществующего
   пользователя), Feign-клиент выбросит исключение `FeignException`. Вы можете
   обработать его, как показано в предыдущем примере, используя
   `@ControllerAdvice`:

```kotlin
@ControllerAdvice
class FeignExceptionHandler {

    @ExceptionHandler(FeignException::class)
    fun handleFeignException(ex: FeignException): ResponseEntity<String> {
        return when (ex.status()) {
            404 -> ResponseEntity("User not found", HttpStatus.NOT_FOUND)
            400 -> ResponseEntity("Invalid request", HttpStatus.BAD_REQUEST)
            else -> ResponseEntity(
                "Server error",
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        }
    }
}
```

7. **Асинхронное использование (опционально)**:
   Если вы хотите вызывать методы асинхронно, можно использовать
   `CompletableFuture` с Feign. Для этого нужно настроить Feign-клиент для
   асинхронных вызовов:

```kotlin
@FeignClient(name = "remote-service", url = "\${remote.service.url}")
interface RemoteServiceClient {

    @GetMapping("/api/users/{id}")
    fun getUserByIdAsync(@PathVariable id: Long): CompletableFuture<UserDto>

    // Другие методы аналогично
}
```

В `UserService` используйте асинхронные вызовы:

```kotlin
@Service
class UserService(
    private val remoteServiceClient: RemoteServiceClient
) {

    fun fetchUserAsync(id: Long): CompletableFuture<UserDto> {
        return remoteServiceClient.getUserByIdAsync(id)
    }
}
```

В контроллере:

```kotlin
@GetMapping("/{id}/async")
fun getUserAsync(@PathVariable id: Long): CompletableFuture<ResponseEntity<UserDto>> {
    return userService.fetchUserAsync(id)
        .thenApply { ResponseEntity.ok(it) }
        .exceptionally {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
}
```

### Дополнительные рекомендации

- **Проверка доступности сервиса**: Убедитесь, что удаленный сервис доступен,
  иначе Feign выбросит исключение.
- **Логирование**: Включите логирование Feign (как показано в `FeignConfig`) для
  отладки.
- **Circuit Breaker**: Для повышения отказоустойчивости можно интегрировать
  библиотеку Resilience4j.
- **Тестирование**: Напишите интеграционные тесты с использованием `WireMock`или
  `TestRestTemplate` для проверки вызовов.

Если у вас есть конкретный сценарий или дополнительные требования (например,
аутентификация, обработка больших данных), дайте знать, и я дополню пример!