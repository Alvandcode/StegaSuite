/**
 * StegaSuite - StegaCrypto
 * © طراحی و اجرا توسط alvandcode - https://github.com/Alvandcode
 * رمزنگاری AES-256-GCM + PBKDF2
 */
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
        require(password.isNotEmpty()) { "پسورد خالی است" }
        val salt = ByteArray(16).also(rng::nextBytes)
        val nonce = ByteArray(12).also(rng::nextBytes)
        val key = derive(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, nonce))
        cipher.updateAAD(MAGIC.toByteArray(Charsets.UTF_8))
        val encrypted = cipher.doFinal(data)
        return MAGIC.toByteArray(Charsets.UTF_8) + byteArrayOf(VERSION.toByte()) + salt + nonce + encrypted
    }

    fun decrypt(blob: ByteArray, password: String): ByteArray {
        require(blob.size > 33) { "داده رمز شده خراب است" }
        require(String(blob.copyOfRange(0, 4), Charsets.UTF_8) == MAGIC) { "داده رمز شده نیست" }
        require(blob[4].toInt() == VERSION) { "نسخه پشتیبانی نمی‌شود" }
        val salt = blob.copyOfRange(5, 21)
        val nonce = blob.copyOfRange(21, 33)
        val encrypted = blob.copyOfRange(33, blob.size)
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, derive(password, salt), GCMParameterSpec(128, nonce))
            cipher.updateAAD(MAGIC.toByteArray(Charsets.UTF_8))
            cipher.doFinal(encrypted)
        } catch (e: Exception) {
            throw IllegalArgumentException("پسورد اشتباه است یا فایل خراب شده")
        }
    }

    private fun derive(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, 256)
        try {
            val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            return SecretKeySpec(bytes, "AES")
        } finally {
            spec.clearPassword()
        }
    }
}
