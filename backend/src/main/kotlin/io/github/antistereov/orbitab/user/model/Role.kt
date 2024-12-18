package io.github.antistereov.orbitab.user.model

enum class Role(private val value: String) {
    USER("USER"),
    ADMIN("ADMIN");

    override fun toString(): String {
        return this.value
    }

    companion object {
        fun fromString(value: String): Role {
            return when (value) {
                "USER" -> USER
                "ADMIN" -> ADMIN
                else -> throw IllegalArgumentException("Failed to create role from string $value")
            }
        }
    }
}