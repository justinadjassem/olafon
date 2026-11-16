package com.olafon.core.data.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject

/**
 * Salted PBKDF2 hashing for short user secrets such as the emergency exit code.
 *
 * Stored form: `v1$<iterations>$<base64 salt>$<base64 hash>`. The plain code is never written to disk.
 */
class SecretHasher @Inject constructor() {
    private val random = SecureRandom()

    fun hash(secret: String): String {
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val derived = derive(secret, salt, ITERATIONS)
        return listOf(VERSION, ITERATIONS.toString(), encode(salt), encode(derived)).joinToString(SEPARATOR)
    }

    fun matches(secret: String, stored: String): Boolean {
        val parsed = parse(stored) ?: return false
        return MessageDigest.isEqual(derive(secret, parsed.salt, parsed.iterations), parsed.hash)
    }

    private fun parse(stored: String): Stored? {
        val parts = stored.split(SEPARATOR)
        val wellFormed = parts.size == FIELD_COUNT && parts[FIELD_VERSION] == VERSION
        val iterations = if (wellFormed) parts[FIELD_ITERATIONS].toIntOrNull() else null
        return iterations?.let { Stored(it, decode(parts[FIELD_SALT]), decode(parts[FIELD_HASH])) }
    }

    private class Stored(val iterations: Int, val salt: ByteArray, val hash: ByteArray)

    private fun derive(secret: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(secret.toCharArray(), salt, iterations, KEY_BITS)
        return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
    }

    private fun encode(bytes: ByteArray): String = Base64.getEncoder().withoutPadding().encodeToString(bytes)

    private fun decode(text: String): ByteArray = Base64.getDecoder().decode(text)

    private companion object {
        const val VERSION = "v1"
        const val SEPARATOR = "$"
        const val FIELD_COUNT = 4
        const val FIELD_VERSION = 0
        const val FIELD_ITERATIONS = 1
        const val FIELD_SALT = 2
        const val FIELD_HASH = 3
        const val ALGORITHM = "PBKDF2WithHmacSHA256"
        const val ITERATIONS = 120_000
        const val SALT_BYTES = 16
        const val KEY_BITS = 256
    }
}
