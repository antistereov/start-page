package io.github.antistereov.start.security

import io.github.antistereov.start.config.properties.EncryptionProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@Component
class AESEncryption(
    private val properties: EncryptionProperties,
) {

    private val logger = LoggerFactory.getLogger(AESEncryption::class.java)

    fun encrypt(strToEncrypt: String): String {
        logger.debug("Encrypting...")

        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        val secretKeySpec = SecretKeySpec(properties.secretKey.toByteArray(), "AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        val encrypted = cipher.doFinal(strToEncrypt.toByteArray())
        return Base64.getUrlEncoder().encodeToString(encrypted)
    }

    fun decrypt(strToDecrypt: String): String {
        logger.debug("Decrypting...")

        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        val secretKeySpec = SecretKeySpec(properties.secretKey.toByteArray(), "AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        val decrypted = cipher.doFinal(Base64.getUrlDecoder().decode(strToDecrypt))
        return String(decrypted)
    }

}