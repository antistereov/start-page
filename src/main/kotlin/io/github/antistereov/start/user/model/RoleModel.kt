package io.github.antistereov.start.user.model

import jakarta.persistence.*

@Entity
@Table(name = "roles")
class RoleModel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String = "",
) {
    @ManyToMany(mappedBy = "roles")
    lateinit var users: Set<UserModel>
}