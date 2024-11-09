package io.github.antistereov.start.user.exception

import java.awt.SystemColor.info

class UsernameAlreadyExistsException(info: String) : UserServiceException(
    message = "$info: Username already exists"
)