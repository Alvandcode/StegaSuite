package com.stegasuite.app

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object StegaCrypto {
    private val rng = SecureRandom()
    private const val MAGIC = "STGS"
    private const val VERSION = 1
    private const val ITERATIONS = 600_000

    fun encrypt(data: ByteArray, password: String): ByteArray {
        require(password.isNotEmpty()) { "Password cannot be empty." }
        val salt = ByteArray(16).also(rng::nextBytes)
        val nonce = ByteArray(12).also(rng::nextBytes)
        val key = derive(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, nonce))
        cipher.updateAAD(MAGIC.toByteArray())
        val encrypted = cipher.doFinal(data)
        return MAGIC.toByteArray() + byteArrayOf(VERSION.toByte()) + salt + nonce + encrypted
    }

    fun decrypt(blob: ByteArray, password: String): ByteArray {
        require(blob.size > 33) { "Invalid encrypted payload." }
        require(String(blob.copyOfRange(0, 4)) == MAGIC) { "Invalid encrypted payload." }
        require(blob[4].toInt() == VERSION) { "Unsupported payload version." }
        val salt = blob.copyOfRange(5, 21)
        val nonce = blob.copyOfRange(21, 33)
        val encrypted = blob.copyOfRange(33, blob.size)
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, derive(password, salt), GCMParameterSpec(128, nonce))
            cipher.updateAAD(MAGIC.toByteArray())
            cipher.doFinal(encrypted)
        } catch (e: Exception) {
            throw IllegalArgumentException("Wrong password or corrupted payload.")
        }
    }

    private fun derive(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, 256)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        return SecretKeySpec(bytes, "AES")
    }
}
