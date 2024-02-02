package io.github.antistereov.start.user.controller

import io.github.antistereov.start.user.dto.UserRequestDTO
import io.github.antistereov.start.user.model.UserModel
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.user.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    @Autowired
    private val userService: UserService
) {
    @PostMapping
    fun createUser(@RequestBody userRequestDTO: UserRequestDTO): ResponseEntity<*> {
        userService.save(userRequestDTO)
        return ResponseEntity.ok("User created successfully.")
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ResponseEntity<*> {
        val user = userService.findById(id)
            ?: return ResponseEntity.notFound().build<Any>()
        return ResponseEntity.ok(user)
    }

    @PutMapping
    fun update(@RequestBody userRequestDTO: UserRequestDTO): ResponseEntity<*> {
        userService.save(userRequestDTO)
        return ResponseEntity.ok("User updated successfully.")
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<*> {
        userService.deleteById(id)
        return ResponseEntity.ok("User deleted successfully.")
    }
}