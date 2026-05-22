// port-lint: ignore — Kotlin-side correctness test for the Sha1 hasher
// API. Upstream behavior comes from tests/mod.rs plus the FIPS 180-2
// SHA-1 reference vectors invoked through digest::dev::fixed_reset_test.

package io.github.kotlinmania.sha1

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class Sha1Test {
    private fun hex(s: String): ByteArray {
        require(s.length % 2 == 0)
        val out = ByteArray(s.length / 2)
        for (i in out.indices) {
            val hi = s[i * 2].digitToInt(16)
            val lo = s[i * 2 + 1].digitToInt(16)
            out[i] = ((hi shl 4) or lo).toByte()
        }
        return out
    }

    @Test
    fun emptyStringMatchesKnownDigest() {
        val out = Sha1.digest(ByteArray(0))
        assertContentEquals(hex("da39a3ee5e6b4b0d3255bfef95601890afd80709"), out)
    }

    @Test
    fun abcMatchesKnownDigest() {
        val out = Sha1.digest("abc".encodeToByteArray())
        assertContentEquals(hex("a9993e364706816aba3e25717850c26c9cd0d89d"), out)
    }

    @Test
    fun longMessageMatchesKnownDigest() {
        val msg = "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq".encodeToByteArray()
        val out = Sha1.digest(msg)
        assertContentEquals(hex("84983e441c3bd26ebaae4aa1f95129e5e54670f1"), out)
    }

    @Test
    fun streamingUpdateMatchesOneShot() {
        val msg = "the quick brown fox jumps over the lazy dog".encodeToByteArray()
        val h = Sha1()
        for (b in msg) {
            h.update(byteArrayOf(b))
        }
        val streamed = h.finalize()
        val oneShot = Sha1.digest(msg)
        assertContentEquals(oneShot, streamed)
    }

    @Test
    fun millionAOnesMatchesKnownDigest() {
        val h = Sha1()
        val chunk = ByteArray(1000) { 'a'.code.toByte() }
        repeat(1000) { h.update(chunk) }
        val out = h.finalize()
        assertContentEquals(hex("34aa973cd4c4daa4f61eeb2bdbad27316534016f"), out)
    }

    @Test
    fun finalizeResetReusesHasher() {
        val h = Sha1()
        h.update("abc".encodeToByteArray())
        val first = h.finalizeReset()
        h.update(ByteArray(0))
        val second = h.finalize()
        assertContentEquals(hex("a9993e364706816aba3e25717850c26c9cd0d89d"), first)
        assertContentEquals(hex("da39a3ee5e6b4b0d3255bfef95601890afd80709"), second)
    }

    @Test
    fun blockBoundaryFlushIsCorrect() {
        // Exactly 64 bytes triggers a single internal block flush plus a
        // padded final block; verify the result matches a one-shot run.
        val msg = ByteArray(64) { (it and 0xFF).toByte() }
        val streamed = Sha1().apply { update(msg) }.finalize()
        val oneShot = Sha1.digest(msg)
        assertContentEquals(oneShot, streamed)
    }

    @Test
    fun coreCopyIsIndependent() {
        val a = Sha1Core()
        a.updateBlocks(arrayOf(ByteArray(64) { 1 }))
        val b = a.copy()
        a.updateBlocks(arrayOf(ByteArray(64) { 2 }))
        assertEquals(false, a.h.contentEquals(b.h))
    }

    @Test
    fun algorithmNameIsSha1() {
        assertEquals("Sha1", Sha1Core().writeAlgName())
    }
}
