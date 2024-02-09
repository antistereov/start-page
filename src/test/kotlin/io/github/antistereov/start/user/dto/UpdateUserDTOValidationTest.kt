package io.github.antistereov.start.user.dto

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@ExtendWith(SpringExtension::class)
@ExtendWith(MockitoExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
internal class UpdateUserDTOValidationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @WithMockUser(username = "admin", authorities = ["ADMIN"])
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
            "Password 1a$" // contains whitespace
        )

        passwords.forEach { password ->
            val updateUserDTO = UpdateUserDTO(
                1L,
                "",
                "",
                "",
                password
            )

            mockMvc.perform(
                MockMvcRequestBuilders
                    .put("/api/users/update")
                    .contentType("application/json")
                    .content(Json.encodeToString(UpdateUserDTO.serializer(), updateUserDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
        }
    }

    @Test
    @WithMockUser(username = "admin", authorities = ["ADMIN"])
    fun `should return 400 Bad Request if email is not valid`() {
        val emails = arrayOf(
            "invalidEmail",     // Missing '@' and domain
            "@invalid.com",     // Missing local-part
            "invalid@.com",     // Missing domain name
            "invalid email@gmail.com" // Contains whitespace
        )

        emails.forEach { email ->
            val updateUserDTO = UpdateUserDTO(
                1L,
                "",
                email,
                "",
                ""
            )

            mockMvc.perform(
                MockMvcRequestBuilders
                    .put("/api/users/update")
                    .contentType("application/json")
                    .content(Json.encodeToString(UpdateUserDTO.serializer(), updateUserDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
        }
    }
}