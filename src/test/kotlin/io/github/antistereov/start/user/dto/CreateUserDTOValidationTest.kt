package io.github.antistereov.start.user.dto

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@ExtendWith(SpringExtension::class)
@ExtendWith(MockitoExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
internal class CreateUserDTOValidationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should return 400 Bad Request if username is blank`() {
        val createUserDto = CreateUserDto("",  "test@gmail.com", "P@ssw0rd", "Test User")

        mockMvc.perform(
            MockMvcRequestBuilders
            .post("/api/users/create")
            .contentType("application/json")
            .content(Json.encodeToString(CreateUserDto.serializer(), createUserDto)))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `should return 400 Bad Request if email is blank`() {
        val createUserDto = CreateUserDto("user", "", "P@ssw0rd", "Test User")

        mockMvc.perform(
            MockMvcRequestBuilders
            .post("/api/users/create")
            .contentType("application/json")
            .content(Json.encodeToString(CreateUserDto.serializer(), createUserDto)))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `should return 400 Bad Request if password is blank`() {
        val createUserDto = CreateUserDto(
            "myusername",
            "test@gmail.com",
            "",
            "Test User"
        )

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/users/create")
                .contentType("application/json")
                .content(Json.encodeToString(CreateUserDto.serializer(), createUserDto)))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `should return 400 Bad Request if name is blank`() {
        val createUserDto = CreateUserDto(
            "myusername",
            "test@gmail.com",
            "Test$123",
            ""
        )

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/users/create")
                .contentType("application/json")
                .content(Json.encodeToString(CreateUserDto.serializer(), createUserDto)))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `should return 400 Bad Request if password does not meet the requirements`() {
        // Test cases with various invalid passwords
        val passwords = arrayOf(
            "password", // does not have uppercase, digit and special character
            "PASSWORD1$", // does not have lowercase
            "Password", // does not have digit and special character
            "Password1", // does not have special character
            "Pa}}word", // does not have digit
            "PASSWORD1a", // does not have special character
            "P@$$", // less than 8 characters
            "Password 1a$", // contains whitespace
        )

        passwords.forEach { password ->
            val createUserDto = CreateUserDto(
                "myusername",
                "test@gmail.com",
                password,
                "Test User"
            )

            mockMvc.perform(
                MockMvcRequestBuilders
                    .post("/api/users/create")
                    .contentType("application/json")
                    .content(Json.encodeToString(CreateUserDto.serializer(), createUserDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
        }
    }

    @Test
    fun `should return 400 Bad Request if email is not valid`() {
        val emails = arrayOf(
            "invalidEmail",     // Missing '@' and domain
            "@invalid.com",     // Missing local-part
            "invalid@.com",     // Missing domain name
            "invalid email@gmail.com" // Contains whitespace
        )

        emails.forEach { email ->
            val createUserDto = CreateUserDto(
                "myusername",
                email,
                "Password$1",
                "Test User"
            )

            mockMvc.perform(
                MockMvcRequestBuilders
                    .post("/api/users/create")
                    .contentType("application/json")
                    .content(Json.encodeToString(CreateUserDto.serializer(), createUserDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
        }
    }
}