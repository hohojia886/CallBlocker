package io.github.hohojia886.callblocker

import io.github.hohojia886.callblocker.util.BackupCryptoManager
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupCryptoManagerTest {

    @Test
    fun testEncryptAndDecrypt_Success() {
        val originalText = "{\"blockedNumbers\":[{\"numberPattern\":\"+886912*\"}]}"
        val password = "MySecretPassword123!"

        val encryptedBytes = BackupCryptoManager.encrypt(originalText, password)
        val decryptedText = BackupCryptoManager.decrypt(encryptedBytes, password)

        assertEquals(originalText, decryptedText)
    }

    @Test(expected = Exception::class)
    fun testDecrypt_WrongPassword_ThrowsException() {
        val originalText = "Secret Database Backup"
        val correctPassword = "CorrectPassword"
        val wrongPassword = "WrongPassword"

        val encryptedBytes = BackupCryptoManager.encrypt(originalText, correctPassword)
        BackupCryptoManager.decrypt(encryptedBytes, wrongPassword)
    }
}
