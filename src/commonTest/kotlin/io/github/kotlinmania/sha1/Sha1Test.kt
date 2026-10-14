// port-lint: tests tests/mod.rs
@file:OptIn(ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.sha1

import io.github.kotlinmania.digest.Digest
import io.github.kotlinmania.digest.dev.feedRand16mib
import io.github.kotlinmania.digest.dev.fixedResetTest
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
    fun helloWorldMatchesDocTestDigest() {
        val hasher = Sha1.new()
        hasher.update("hello world".encodeToByteArray())
        val result = hasher.finalize()
        assertContentEquals(hex("2aae6c35c94fcfb415dbe95f408b9ce91ee846ed"), result)
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
    fun hasherCopyPreservesPartialBufferAndCoreState() {
        val original = Sha1.newWithPrefix("The quick brown fox ".encodeToByteArray())
        val cloned = original.copy()
        original.update("jumps over the lazy dog".encodeToByteArray())
        cloned.update("jumps over the lazy cog".encodeToByteArray())
        assertContentEquals(
            hex("2fd4e1c67a2d28fced849ee1bb76e7391b93eb12"),
            original.finalize(),
        )
        assertContentEquals(
            hex("de9f2c7fd25e1b3afad3e85a0bd17d9b100db4b3"),
            cloned.finalize(),
        )
    }

    @Test
    fun algorithmNameAndOidAndFormatMatchUpstream() {
        assertEquals("Sha1", Sha1Core().writeAlgName())
        assertEquals("1.3.14.3.2.26", Sha1Core.OID)
        assertEquals("1.3.14.3.2.26", Sha1.OID)
        assertEquals("Sha1Core { ... }", Sha1Core().toString())
        assertEquals("Sha1 { .. }", Sha1().toString())
    }

    @Test
    fun digestFactoryRegistrationWorks() {
        val out = Digest.digest(Sha1::class, "abc".encodeToByteArray())
        assertContentEquals(hex("a9993e364706816aba3e25717850c26c9cd0d89d"), out)
        assertEquals(20, Digest.outputSize(Sha1::class))
        assertEquals(64, Digest.blockSize(Sha1::class))
    }

    @Test
    fun topLevelCompressReExportMatchesBackend() {
        val state = uintArrayOf(0x67452301u, 0xEFCDAB89u, 0x98BADCFEu, 0x10325476u, 0xC3D2E1F0u)
        val block = ByteArray(64)
        block[0] = 0x80.toByte()
        compress(state, arrayOf(block))
        assertContentEquals(
            uintArrayOf(0xDA39A3EEu, 0x5E6B4B0Du, 0x3255BFEFu, 0x95601890u, 0xAFD80709u),
            state,
        )
    }

    @Test
    fun sha1Rand() {
        val h = Sha1.new()
        feedRand16mib(h)
        assertContentEquals(
            hex("7e565a25a8b123e9881addbcedcd927b23377a78"),
            h.finalize(),
        )
    }

    @Test
    fun sha1Main() {
        for ((index, vector) in fixedResetVectors.withIndex()) {
            assertNull(
                fixedResetTest(vector.input, vector.output, ::Sha1, Sha1::copy),
                "failed upstream test vector $index in sha1Main",
            )
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
