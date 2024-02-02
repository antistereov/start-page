package io.github.antistereov.start.user.controller

import io.github.antistereov.start.user.dto.UserCreateDTO
import io.github.antistereov.start.user.dto.UserResponseDTO
import io.github.antistereov.start.user.dto.UserUpdateDTO
import io.github.antistereov.start.user.service.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

internal class UserControllerTest {

    @Mock
    private lateinit var userService: UserService

    private lateinit var userController: UserController

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        userController = UserController(userService)
    }

    @Test
    fun `Test createUser`() {
        val user = UserCreateDTO("username", "email@domain.com", "password", "name")

        Mockito.`when`(userService.create(Mockito.any(UserCreateDTO::class.java)))
            .thenReturn(Unit)

        val result: ResponseEntity<*> = userController.createUser(user)
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals("User ${user.username} created successfully.", result.body)
    }

    @Test
    fun `Test createAdmin`() {
        val user = UserCreateDTO("admin", "admin@domain.com", "password", "administrator")

        Mockito.`when`(userService.createAdmin(Mockito.any(UserCreateDTO::class.java)))
            .thenReturn(Unit)

        val result: ResponseEntity<*> = userController.createAdmin(user)
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals("User ${user.username} created successfully.", result.body)
    }

    @Test
    fun `Test getUser`() {
        val id: Long = 1


        val user = UserResponseDTO(1, "username", "email@domain.com", "name")

        Mockito.`when`(userService.findById(id))
            .thenReturn(user)

        val result: ResponseEntity<*> = userController.getUser(id)
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(user, result.body)
    }

    @Test
    fun `Test update`() {
        val user = UserUpdateDTO(1, "new_username", "new_email@domain.com", "new_name")

        Mockito.`when`(userService.update(Mockito.any(UserUpdateDTO::class.java)))
            .thenReturn(Unit)

        val result: ResponseEntity<*> = userController.update(user)
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals("User ${user.username} updated successfully.", result.body)
    }

    @Test
    fun `Test delete`() {
        var id: Long = 1

        Mockito.`when`(userService.deleteById(id))
            .thenReturn(true)

        val result: ResponseEntity<*> = userController.delete(id)
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals("User $id deleted successfully.", result.body)
    }
}