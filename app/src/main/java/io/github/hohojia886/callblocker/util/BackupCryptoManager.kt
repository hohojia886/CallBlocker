/**
 * AES-256-GCM + PBKDF2 encryption manager for local backup and restoration of database files.
 */
package io.github.hohojia886.callblocker.util

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupCryptoManager {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val KEY_SIZE = 256
    private const val ITERATIONS = 600_000
    private const val SALT_SIZE_BYTES = 16
    private const val IV_SIZE_BYTES = 12
    private const val TAG_SIZE_BITS = 128

    /** Encrypts JSON string payload using password-derived AES-256-GCM key. */
    fun encrypt(jsonContent: String, password: String): ByteArray {
        val salt = ByteArray(SALT_SIZE_BYTES)
        val iv = ByteArray(IV_SIZE_BYTES)
        val random = SecureRandom()
        random.nextBytes(salt)
        random.nextBytes(iv)

        val secretKey = deriveKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        val gcmSpec = GCMParameterSpec(TAG_SIZE_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val ciphertext = cipher.doFinal(jsonContent.toByteArray(Charsets.UTF_8))

        return salt + iv + ciphertext
    }

    /** Decrypts `.spamdb` binary payload using password-derived AES-256-GCM key. */
    fun decrypt(encryptedBytes: ByteArray, password: String): String {
        require(encryptedBytes.size > SALT_SIZE_BYTES + IV_SIZE_BYTES) {
            "Invalid backup file format"
        }

        val salt = encryptedBytes.copyOfRange(0, SALT_SIZE_BYTES)
        val iv = encryptedBytes.copyOfRange(SALT_SIZE_BYTES, SALT_SIZE_BYTES + IV_SIZE_BYTES)
        val ciphertext = encryptedBytes.copyOfRange(SALT_SIZE_BYTES + IV_SIZE_BYTES, encryptedBytes.size)

        val secretKey = deriveKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        val gcmSpec = GCMParameterSpec(TAG_SIZE_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

        val decryptedBytes = cipher.doFinal(ciphertext)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /** Derives secret key from password using PBKDF2WithHmacSHA256 and salt. */
    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_SIZE)
        val tmpKey = factory.generateSecret(spec)
        return SecretKeySpec(tmpKey.encoded, "AES")
    }
}
