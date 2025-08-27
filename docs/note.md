# Заметки по проекту

## `27.08`:

> Два сервиса можно расширить добавлением:

- Аутентификации
- Retry-механизмов
- Circuit Breaker (например, с Resilience4j)
- Более сложной обработки ошибок
- Асинхронных вызовов

---
> Асинхронное использование (опционально):

Можно использовать CompletableFuture с Feign. Для этого нужно настроить
Feign-клиент для асинхронных вызовов:

```kotlin
@FeignClient(name = "remote-service", url = "\${remote.service.url}")
interface RemoteServiceClient {

    @GetMapping("/api/users/{id}")
    fun getUserByIdAsync(@PathVariable id: Long): CompletableFuture<UserDto>

    // Другие методы аналогично
}
```

В UserService используйте асинхронные вызовы:

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

> Дополнительные рекомендации

- Проверка доступности сервиса: Убедитесь, что удаленный сервис доступен, иначе
  Feign выбросит исключение.
- Логирование: Включите логирование Feign (как показано в FeignConfig) для
  отладки.
- Circuit Breaker: Для повышения отказоустойчивости можно интегрировать
  библиотеку Resilience4j.
- Тестирование: Напишите интеграционные тесты с использованием WireMock или
  TestRestTemplate для проверки вызовов.