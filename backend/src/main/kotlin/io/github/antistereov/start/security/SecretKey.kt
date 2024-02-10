package io.github.antistereov.start.security

import java.util.*
import javax.crypto.KeyGenerator

fun generateSecretKey(): String {
    val keyGenerator = KeyGenerator.getInstance("AES")
    keyGenerator.init(128) //key size: could be 128, 192, or 256 for AES
    val secretKey = keyGenerator.generateKey()
    return Base64.getEncoder().encodeToString(secretKey.encoded)
}

fun main() {
    println(generateSecretKey())

    val aes = AESEncryption()
    val original = "Test string"
    val encrypted = aes.encrypt(original)
    val decrypted = aes.decrypt(encrypted)
    println("Original: $original")
    println("Encrypted: $encrypted")
    println("Decrypted: $decrypted")

    val key = "3LW7qK4mP7u8vgBMA8QgnQ=="
    val decodedKey = Base64.getDecoder().decode(key)
    println("Decoded key length is: ${decodedKey.size}")
}