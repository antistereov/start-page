package io.github.antistereov.start.user.controller

import io.github.antistereov.start.user.dto.UserResponseDTO
import io.github.antistereov.start.user.service.UserService
import io.mockk.every
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print

@WithMockUser(username = "admin", authorities = ["ADMIN"])
@WebMvcTest(controllers = [UserController::class])
internal class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var userService: UserService

    @Test
    fun `only admin can access users endpoint`() {

        // TODO

        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/api/users")
        )
    }

    @Test
    fun `getUser returns user when user exists`() {
        val userId = 1L
        val userResponseDTO = UserResponseDTO(
            "test",
            "test@email.com",
            "Test User"
        )

        Mockito.`when`(userService.findById(userId)).thenReturn(userResponseDTO)

        // Perform request
        mockMvc.perform(MockMvcRequestBuilders
            .get("/api/users/$userId"))
            .andDo(print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content()
                .json(
                    Json.encodeToString(
                        UserResponseDTO.serializer(), userResponseDTO
                    )
                )
            )
    }

    @Test
    fun `getUser returns not found when user is not found`() {
        val userId = 1L
        Mockito.`when`(userService.findById(userId)).thenReturn(null)

        mockMvc.perform(MockMvcRequestBuilders
            .get("/api/users/$userId"))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.content().string("User not found."))
    }
}