package io.github.antistereov.start.util

import org.springframework.beans.factory.annotation.Value
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object AESEncryption {

    @Value("\${aes.encryptionkey}")
    lateinit var key: String

    private val cipherInstance = "AES/ECB/PKCS5Padding"

    fun encrypt(inputString: String): String {
        val secretKey = SecretKeySpec(key.toByteArray(), "AES")
        val cipher = Cipher.getInstance(cipherInstance)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        return Base64.getEncoder().encodeToString(cipher.doFinal(inputString.toByteArray()))
    }

    fun decrypt(token: String): String {
        val secretKey = SecretKeySpec(key.toByteArray(), "AES")
        val cipher = Cipher.getInstance(cipherInstance)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        return String(cipher.doFinal(Base64.getDecoder().decode(token)))
    }
}