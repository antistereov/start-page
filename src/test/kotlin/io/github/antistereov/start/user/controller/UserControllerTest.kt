package io.github.antistereov.start.user.controller

import io.github.antistereov.start.user.dto.CreateUserDto
import io.github.antistereov.start.user.dto.UpdateUserDTO
import io.github.antistereov.start.user.dto.UserResponseDTO
import io.github.antistereov.start.user.model.UserModel
import io.github.antistereov.start.user.service.UserService
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity.post
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension::class)
@ExtendWith(MockitoExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
internal class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var userService: UserService


    @Test
    @WithMockUser(username = "admin", authorities = ["ADMIN"])
    fun `getUser returns user when user exists`() {
        val userId = 1L
        val userResponseDTO = UserResponseDTO("test", "test@email.com", "Test User")

        Mockito.`when`(userService.findById(userId)).thenReturn(userResponseDTO)

        // Perform request
        mockMvc.perform(MockMvcRequestBuilders
            .get("/api/users/$userId"))
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content()
                .json(
                    Json.encodeToString(UserResponseDTO.serializer(), userResponseDTO)
                )
            )
    }

    @Test
    @WithMockUser(username = "admin", authorities = ["ADMIN"])
    fun `getUser returns not found when user is not found`() {
        val userId = 1L
        Mockito.`when`(userService.findById(userId)).thenReturn(null)

        mockMvc.perform(MockMvcRequestBuilders
            .get("/api/users/$userId"))
            .andExpect(status().isNotFound)
            .andExpect(MockMvcResultMatchers.content().string("User not found."))
    }


    @Test
    fun `createUser creates new user`() {
        val createUserDto = CreateUserDto("test", "test@email.com", "Password0.#", "Test User")
        val userResponseDTO = UserResponseDTO("test","test@email.com","Test User")
        val user = UserModel(1L,"test","test@email.com","Password0.#", "Test User")

        Mockito.`when`(userService.createUser(createUserDto)).thenReturn(user)

        mockMvc.perform(MockMvcRequestBuilders
            .post("/api/users/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(Json.encodeToString(CreateUserDto.serializer(), createUserDto)))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content()
                .json(
                    Json.encodeToString(UserResponseDTO.serializer(), userResponseDTO)
                )
            )
    }

    @Test
    @WithMockUser(username = "admin", authorities = ["ADMIN"])
    fun `updateUser updates user`() {
        val updateUserDTO = UpdateUserDTO(1L, "test")
        val user = UserModel(1L,"test","test@email.com","Password0.#", "Test User")
        val userResponseDTO = UserResponseDTO("test","test@email.com","Test User")

        Mockito.`when`(userService.update(updateUserDTO)).thenReturn(user)

        mockMvc.perform(MockMvcRequestBuilders
            .put("/api/users/update")
            .contentType(MediaType.APPLICATION_JSON)
            .content(Json.encodeToString(UpdateUserDTO.serializer(), updateUserDTO)))
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content()
                .json(
                    Json.encodeToString(UserResponseDTO.serializer(), userResponseDTO)
                )
            )
    }

    @Test
    @WithMockUser(username = "admin", authorities = ["ADMIN"])
    fun `delete user - success`() {
        val id = 1L
        Mockito.`when`(userService.deleteById(id)).thenReturn(true)

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/$id"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string("User $id deleted successfully."))
    }

    @Test
    @WithMockUser(username = "admin", authorities = ["ADMIN"])
    fun `delete user - not found`() {
        val id = 1L
        Mockito.`when`(userService.deleteById(id)).thenReturn(false)

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/$id"))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.content().string("User not found."))
    }
}