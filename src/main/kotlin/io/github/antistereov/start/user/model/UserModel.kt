package io.github.antistereov.start.user.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
class UserModel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, unique = true)
    var username: String = "",

    @Column(nullable = false, unique = true)
    var email: String = "",

    @Column(nullable = false)
    var password: String = "",

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false)
    var isEnabled: Boolean = true,

    @Column(nullable = false)
    var isCredentialsNonExpired: Boolean = true,

    @Column(nullable = false)
    var isAccountNonLocked: Boolean = true,

    @Column(nullable = false)
    var isAccountNonExpired: Boolean = true,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles: Set<RoleModel> = emptySet()
)