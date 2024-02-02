package io.github.antistereov.start.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(nullable = false)
    var name: String = ""

    @Column(nullable = false)
    var email: String = ""

    @Column(nullable = false)
    var password: String = ""
}