package io.github.antistereov.start.user.controller

import io.github.antistereov.start.user.dto.UserRequestDTO
import io.github.antistereov.start.user.dto.UserUpdateDTO
import io.github.antistereov.start.user.service.UserService
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
    fun createUser(@RequestBody userRequestDTO: UserRequestDTO): ResponseEntity<*> {
        return try {
            userService.create(userRequestDTO)
            ResponseEntity.ok("User ${userRequestDTO.username} created successfully.")
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
    }

    @PostMapping("/admin")
    fun createAdmin(@RequestBody userRequestDTO: UserRequestDTO): ResponseEntity<*> {
        return try {
            userService.createAdmin(userRequestDTO)
            ResponseEntity.ok("User ${userRequestDTO.username} created successfully.")
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ResponseEntity<*> {
        val user = userService.findById(id)
            ?: return ResponseEntity.notFound().build<Any>()
        return ResponseEntity.ok(user)
    }


    @PutMapping("/update")
    fun update(@RequestBody userUpdateDTO: UserUpdateDTO): ResponseEntity<*> {
        return try {
            userService.update(userUpdateDTO)
            return ResponseEntity.ok("User ${userUpdateDTO.username} updated successfully.")
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