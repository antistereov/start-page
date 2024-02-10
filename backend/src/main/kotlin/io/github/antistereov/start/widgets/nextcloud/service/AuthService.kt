package io.github.antistereov.start.widgets.nextcloud.service

import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCredentials
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val aesEncryption: AESEncryption,
) {

    fun getCredentials(userId: String): NextcloudCredentials {
        val user = userRepository.findById(userId).orElseThrow { throw RuntimeException("User not found") }
        return NextcloudCredentials(
            aesEncryption.decrypt(user.nextcloudHost
                ?: throw RuntimeException("No news URL found for user $userId.")),
            aesEncryption.decrypt(user.nextcloudUsername
                ?: throw RuntimeException("No news URL found for user $userId.")),
            aesEncryption.decrypt(user.nextcloudPassword
                ?: throw RuntimeException("No news URL found for user $userId.")),
        )
    }

    fun authentication(userId: String, nextcloudCredentials: NextcloudCredentials) {
        val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found") }

        val url = if (nextcloudCredentials.url.endsWith("/")) {
            nextcloudCredentials.url.substring(0, nextcloudCredentials.url.length - 1)
        } else {
            nextcloudCredentials.url
        }

        user.nextcloudHost = aesEncryption.encrypt(url)
        user.nextcloudUsername = aesEncryption.encrypt(nextcloudCredentials.username)
        // TODO remove encryption for password since it will be encoded in frontend already
        user.nextcloudPassword = aesEncryption.encrypt(nextcloudCredentials.password)

        userRepository.save(user)
    }
}