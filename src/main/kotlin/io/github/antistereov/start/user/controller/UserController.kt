package io.github.antistereov.start.user.controller

import io.github.antistereov.start.user.dto.CreateUserDto
import io.github.antistereov.start.user.dto.UpdateUserDTO
import io.github.antistereov.start.user.model.UserModel
import io.github.antistereov.start.user.service.UserService
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    @Autowired
    private val userService: UserService
) {
    @PostMapping("/create")
    fun createUser(@RequestBody @Valid createUserDto: CreateUserDto): ResponseEntity<*> {
        return try {
            val user: UserModel = userService.createUser(createUserDto)
            ResponseEntity.ok(user)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
    }

    @PostMapping("/admin")
    fun createAdmin(@RequestBody @Valid createUserDto: CreateUserDto): ResponseEntity<*> {
        return try {
            val user: UserModel = userService.createAdmin(createUserDto)
            ResponseEntity.ok(user)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ResponseEntity<*> {
        val user = userService.findById(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.")
        return ResponseEntity.ok(user)
    }


    @PutMapping("/update")
    fun update(@RequestBody @Valid updateUserDTO: UpdateUserDTO): ResponseEntity<*> {
        return try {
            val user: UserModel = userService.update(updateUserDTO)
            return ResponseEntity.ok(user)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<*> {
        return if (userService.deleteById(id)) {
            ResponseEntity.ok("User $id deleted successfully.")
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.")
        }
    }
}