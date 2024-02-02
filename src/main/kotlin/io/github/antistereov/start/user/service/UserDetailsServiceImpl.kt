package io.github.antistereov.start.user.service

import io.github.antistereov.start.user.model.UserDetailsImpl
import io.github.antistereov.start.user.model.UserModel
import io.github.antistereov.start.user.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val userRepository: UserRepository,
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user: UserModel = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User with username $username not found")

        return UserDetailsImpl.build(user)
    }
}