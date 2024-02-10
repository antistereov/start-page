package io.github.antistereov.start.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Component
class AESEncryption {

    @Value("\${encryption.secretKey}")
    private lateinit var secretKey: String

    fun encrypt(strToEncrypt: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), "AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        val encrypted = cipher.doFinal(strToEncrypt.toByteArray())
        return Base64.getUrlEncoder().encodeToString(encrypted)
    }

    fun decrypt(strToDecrypt: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), "AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        val decrypted = cipher.doFinal(Base64.getUrlDecoder().decode(strToDecrypt))
        return String(decrypted)
    }

}