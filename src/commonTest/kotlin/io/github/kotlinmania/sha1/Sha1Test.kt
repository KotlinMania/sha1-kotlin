// port-lint: tests tmp/sha1/tests/mod.rs

package io.github.kotlinmania.sha1

import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

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

    @Test
    fun sha1Rand() {
        assertContentEquals(
            hex("7e565a25a8b123e9881addbcedcd927b23377a78"),
            random16MiBDigest(),
        )
    }

    @Test
    fun sha1Main() {
        for ((index, vector) in fixedResetVectors.withIndex()) {
            assertNull(
                fixedResetTest(vector.input, vector.output),
                "failed upstream test vector $index in sha1Main",
            )
        }
    }

    private fun fixedResetTest(input: ByteArray, expected: ByteArray): String? {
        val h = Sha1()
        h.update(input)
        if (!h.finalize().contentEquals(expected)) {
            return "whole message"
        }

        val hWithReset = Sha1()
        hWithReset.update(input)
        if (!hWithReset.finalizeReset().contentEquals(expected)) {
            return "whole message after reset"
        }

        for (chunkSize in 1 until min(17, input.size)) {
            val streamingHasher = Sha1()
            val streamingHasherWithReset = Sha1()
            var start = 0
            while (start < input.size) {
                val end = min(start + chunkSize, input.size)
                val chunk = input.copyOfRange(start, end)
                streamingHasher.update(chunk)
                streamingHasherWithReset.update(chunk)
                start = end
            }
            if (!streamingHasher.finalize().contentEquals(expected)) {
                return "message in chunks"
            }
            if (!streamingHasherWithReset.finalizeReset().contentEquals(expected)) {
                return "message in chunks after reset"
            }
        }

        return null
    }

    private fun random16MiBDigest(): ByteArray {
        val hasher = Sha1()
        val buf = ByteArray(1024)
        val rng = XorShiftRng()
        val rounds = 16 * (1 shl 20) / buf.size
        repeat(rounds) {
            rng.fill(buf)
            hasher.update(buf)
            hasher.update(byteArrayOf(42))
        }
        return hasher.finalize()
    }

    private class XorShiftRng {
        private var x = 0x07873B4Au
        private var y = 0xFAAB8FFEu
        private var z = 0x1745980Fu
        private var w = 0xB0ADB4F3u

        fun fill(buf: ByteArray) {
            var i = 0
            while (i < buf.size) {
                val value = nextUInt()
                buf[i] = value.toByte()
                buf[i + 1] = (value shr 8).toByte()
                buf[i + 2] = (value shr 16).toByte()
                buf[i + 3] = (value shr 24).toByte()
                i += 4
            }
        }

        private fun nextUInt(): UInt {
            val t = x xor (x shl 11)
            x = y
            y = z
            z = w
            w = w xor (w shr 19) xor (t xor (t shr 8))
            return w
        }
    }

    private data class FixedResetVector(
        val input: ByteArray,
        val output: ByteArray,
    )

    private val fixedResetVectors =
        listOf(
            FixedResetVector(
                "abc".encodeToByteArray(),
                hex("a9993e364706816aba3e25717850c26c9cd0d89d"),
            ),
            FixedResetVector(
                "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq".encodeToByteArray(),
                hex("84983e441c3bd26ebaae4aa1f95129e5e54670f1"),
            ),
            FixedResetVector(
                "The quick brown fox jumps over the lazy dog".encodeToByteArray(),
                hex("2fd4e1c67a2d28fced849ee1bb76e7391b93eb12"),
            ),
            FixedResetVector(
                "The quick brown fox jumps over the lazy cog".encodeToByteArray(),
                hex("de9f2c7fd25e1b3afad3e85a0bd17d9b100db4b3"),
            ),
        )
}
