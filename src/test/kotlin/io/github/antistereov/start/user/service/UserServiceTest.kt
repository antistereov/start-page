package io.github.antistereov.start.user.service

import io.github.antistereov.start.user.dto.CreateUserDto
import io.github.antistereov.start.user.dto.UpdateUserDTO
import io.github.antistereov.start.user.model.UserModel
import io.github.antistereov.start.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@ExtendWith(SpringExtension::class)
@ExtendWith(MockitoExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
internal class UserControllerTest {

    @Autowired
    private lateinit var userService: UserService

    @MockBean
    private lateinit var userRepository: UserRepository

    @MockBean
    private lateinit var passwordEncoder: BCryptPasswordEncoder

    // Define a user model object that can be used in each test
    private val baseUser = UserModel(
        id = 1L,
        username = "test_username",
        email = "test@gmail.com",
        name = "Test User",
        password = "test_password"
    )

    @BeforeEach
    fun setup() {
        // Reset the user object before each test
        baseUser.username = "test_username"
        baseUser.email = "test@gmail.com"
        baseUser.name = "Test User"
        baseUser.password = "test_password"
    }

    @Test
    fun `create user - username already exists` () {
        val createUserDto = CreateUserDto("test", "test@email.com", "Password0.#", "Test User")
        Mockito.`when`(userRepository.existsByUsername(createUserDto.username)).thenReturn(true)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            userService.createUser(createUserDto)
        }

        assert(exception.message == "Username already exists")
    }

    @Test
    fun `create user - email already in use` () {
        val createUserDto = CreateUserDto("test", "test@email.com", "Password0.#", "Test User")
        Mockito.`when`(userRepository.existsByUsername(createUserDto.username)).thenReturn(false)
        Mockito.`when`(userRepository.existsByEmail(createUserDto.email)).thenReturn(true)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            userService.createUser(createUserDto)
        }

        assert(exception.message == "Email is already in use")
    }

    @Test
    fun `createUser - failed to save user`() {
        val createUserDto = CreateUserDto("test", "test@email.com", "Password0.#", "Test User")
        val user = UserModel(1L,"test", "test@email.com", "EncodedPassword", "Test User")

        Mockito.`when`(userRepository.existsByUsername(createUserDto.username)).thenReturn(false)
        Mockito.`when`(userRepository.existsByEmail(createUserDto.email)).thenReturn(false)
        Mockito.`when`(passwordEncoder.encode(createUserDto.password)).thenReturn("EncodedPassword")
        Mockito.`when`(userRepository.save(Mockito.any(UserModel::class.java))).thenThrow(DataIntegrityViolationException::class.java)

        val exception = assertThrows<IllegalArgumentException>("Failed to create user") {
            userService.createUser(createUserDto)
        }
        assertEquals("Failed to create user", exception.message)
    }

    @Test
    fun `should update username successfully` () {
        val updateUserDTO = UpdateUserDTO(
            id = 1L,
            username = "updated_username"
        )

        Mockito.`when`(userRepository.findById(updateUserDTO.id)).thenReturn(Optional.of(baseUser))
        Mockito.`when`(userRepository.existsByUsername(updateUserDTO.username!!)).thenReturn(false)

        userService.update(updateUserDTO)

        verify(userRepository, times(1)).save(baseUser)
        assertEquals(updateUserDTO.username, baseUser.username)
    }

    @Test
    fun `should throw exception when update with existing username` () {
        val updateUserDTO = UpdateUserDTO(
            id = 1L,
            username = "existing_username"
        )

        Mockito.`when`(userRepository.findById(updateUserDTO.id)).thenReturn(Optional.of(baseUser))
        Mockito.`when`(userRepository.existsByUsername(updateUserDTO.username!!)).thenReturn(true)

        assertThrows<IllegalArgumentException> { userService.update(updateUserDTO) }
    }

    @Test
    fun `should update email successfully` () {
        val updateUserDTO = UpdateUserDTO(
            id = 1L,
            email = "updated_email@gmail.com"
        )

        Mockito.`when`(userRepository.findById(updateUserDTO.id)).thenReturn(Optional.of(baseUser))
        Mockito.`when`(userRepository.existsByEmail(updateUserDTO.email!!)).thenReturn(false)

        userService.update(updateUserDTO)

        verify(userRepository, times(1)).save(baseUser)
        assertEquals(updateUserDTO.email, baseUser.email)
    }

    @Test
    fun `should throw exception when update with existing email` () {
        val updateUserDTO = UpdateUserDTO(
            id = 1L,
            email = "existing_email@gmail.com"
        )

        Mockito.`when`(userRepository.findById(updateUserDTO.id)).thenReturn(Optional.of(baseUser))
        Mockito.`when`(userRepository.existsByEmail(updateUserDTO.email!!)).thenReturn(true)

        assertThrows<IllegalArgumentException> { userService.update(updateUserDTO) }
    }

    @Test
    fun `should update name successfully` () {
        val updateUserDTO = UpdateUserDTO(
            id = 1L,
            name = "Updated Name"
        )

        Mockito.`when`(userRepository.findById(updateUserDTO.id)).thenReturn(Optional.of(baseUser))

        userService.update(updateUserDTO)

        verify(userRepository, times(1)).save(baseUser)
        assertEquals(updateUserDTO.name, baseUser.name)
    }

    @Test
    fun `should update password successfully` () {
        val updateUserDTO = UpdateUserDTO(
            id = 1L,
            password = "updated_password"
        )

        val encodedPassword = "encoded_updated_password"

        Mockito.`when`(userRepository.findById(updateUserDTO.id)).thenReturn(Optional.of(baseUser))
        Mockito.`when`(passwordEncoder.encode(updateUserDTO.password)).thenReturn(encodedPassword)

        userService.update(updateUserDTO)

        verify(userRepository, times(1)).save(baseUser)
        assertEquals(encodedPassword, baseUser.password)
    }

    @Test
    fun `should throw exception when no fields to update provided` () {
        val updateUserDTO = UpdateUserDTO(
            id = 1L,
            null,
            null,
            null,
            null
        )

        Mockito.`when`(userRepository.findById(updateUserDTO.id)).thenReturn(Optional.of(baseUser))

        assertThrows<IllegalArgumentException> { userService.update(updateUserDTO) }
    }
}