package io.github.antistereov.start.user.model

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.stream.Collectors

class UserDetailsImpl(
    private val user: UserModel
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority?> {
        return user.roles.stream()
            .map { role -> SimpleGrantedAuthority(role.name) }
            .collect(Collectors.toList())
    }

    override fun getPassword(): String {
        return user.password
    }

    override fun getUsername(): String {
        return user.username
    }

    override fun isEnabled(): Boolean {
        return user.isEnabled
    }

    override fun isCredentialsNonExpired(): Boolean {
        return user.isCredentialsNonExpired
    }

    override fun isAccountNonLocked(): Boolean {
        return user.isAccountNonLocked
    }

    override fun isAccountNonExpired(): Boolean {
        return user.isAccountNonExpired
    }

    companion object {
        fun build(user: UserModel): UserDetailsImpl {
            return UserDetailsImpl(user)
        }
    }
}