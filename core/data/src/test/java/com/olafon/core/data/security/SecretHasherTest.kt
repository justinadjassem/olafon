package com.olafon.core.data.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecretHasherTest {
    private val hasher = SecretHasher()

    @Test
    fun `the right code matches its hash`() {
        val stored = hasher.hash("4821")
        assertTrue(hasher.matches("4821", stored))
    }

    @Test
    fun `a wrong code does not match`() {
        val stored = hasher.hash("4821")
        assertFalse(hasher.matches("4822", stored))
        assertFalse(hasher.matches("", stored))
    }

    @Test
    fun `hashing the same code twice gives different salts`() {
        assertNotEquals(hasher.hash("4821"), hasher.hash("4821"))
    }

    @Test
    fun `the stored form never contains the code`() {
        assertFalse(hasher.hash("secret-code").contains("secret-code"))
    }

    @Test
    fun `garbage stored values never match`() {
        assertFalse(hasher.matches("4821", ""))
        assertFalse(hasher.matches("4821", "v1\$abc\$def\$ghi"))
        assertFalse(hasher.matches("4821", "v9\$120000\$AAAA\$BBBB"))
        assertFalse(hasher.matches("4821", "4821"))
    }
}
