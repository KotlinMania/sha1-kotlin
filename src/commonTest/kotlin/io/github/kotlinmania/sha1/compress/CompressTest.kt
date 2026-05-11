// port-lint: ignore — Kotlin-side correctness test for the ported compression
// function. No matching `.rs` source; the upstream behavior comes from
// `tmp/sha1/src/compress/soft.rs` and standard SHA-1 test vectors.
@file:OptIn(ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.sha1.compress

import kotlin.test.Test
import kotlin.test.assertEquals

class CompressTest {
    private fun initialState(): UIntArray =
        uintArrayOf(0x67452301u, 0xEFCDAB89u, 0x98BADCFEu, 0x10325476u, 0xC3D2E1F0u)

    private fun padSingleBlock(message: ByteArray): ByteArray {
        require(message.size < 56) { "single-block helper assumes message fits in one padded block" }
        val block = ByteArray(BLOCK_SIZE)
        message.copyInto(block)
        block[message.size] = 0x80.toByte()
        val bitLen: Long = message.size.toLong() * 8
        for (i in 0 until 8) {
            block[BLOCK_SIZE - 1 - i] = ((bitLen ushr (i * 8)) and 0xFFL).toByte()
        }
        return block
    }

    @Test
    fun emptyStringMatchesKnownDigest() {
        val state = initialState()
        compress(state, arrayOf(padSingleBlock(ByteArray(0))))
        assertEquals(0xDA39A3EEu, state[0])
        assertEquals(0x5E6B4B0Du, state[1])
        assertEquals(0x3255BFEFu, state[2])
        assertEquals(0x95601890u, state[3])
        assertEquals(0xAFD80709u, state[4])
    }

    @Test
    fun abcMatchesKnownDigest() {
        val state = initialState()
        compress(state, arrayOf(padSingleBlock(byteArrayOf(0x61, 0x62, 0x63))))
        assertEquals(0xA9993E36u, state[0])
        assertEquals(0x4706816Au, state[1])
        assertEquals(0xBA3E2571u, state[2])
        assertEquals(0x7850C26Cu, state[3])
        assertEquals(0x9CD0D89Du, state[4])
    }
}
