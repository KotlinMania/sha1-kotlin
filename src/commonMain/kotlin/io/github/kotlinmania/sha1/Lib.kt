// port-lint: source lib.rs
@file:OptIn(ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.sha1

import io.github.kotlinmania.sha1.compress.BLOCK_SIZE
import io.github.kotlinmania.sha1.compress.compress

// Pure Kotlin implementation of the SHA-1 cryptographic hash algorithm.
// SHA-1 is cryptographically broken and unsuitable for security-sensitive
// use; this module exists for legacy interoperability.

internal const val STATE_LEN: Int = 5

internal val SHA1_INITIAL_STATE: UIntArray =
    uintArrayOf(
        0x67452301u,
        0xEFCDAB89u,
        0x98BADCFEu,
        0x10325476u,
        0xC3D2E1F0u,
    )

internal const val SHA1_OUTPUT_SIZE: Int = 20

object BlockSize {
    const val BYTES: Int = BLOCK_SIZE
}

object BufferKind {
    const val EAGER: Boolean = true
}

object OutputSize {
    const val BYTES: Int = SHA1_OUTPUT_SIZE
}

/**
 * Core SHA-1 state.
 *
 * The core owns the five running words and the number of complete blocks that
 * have already been compressed. It only accepts full 64-byte blocks; [Sha1]
 * owns the partial block buffer and final padding.
 */
class Sha1Core {
    internal val h: UIntArray = SHA1_INITIAL_STATE.copyOf()
    internal var blockLen: ULong = 0u

    internal val blockSize: Int get() = BLOCK_SIZE
    internal val outputSize: Int get() = SHA1_OUTPUT_SIZE

    // Consume whole 64-byte blocks. The caller owns partial-block buffering.
    fun updateBlocks(blocks: Array<ByteArray>) {
        blockLen += blocks.size.toULong()
        compress(h, blocks)
    }

    // Apply SHA-style length padding and emit the final 20-byte digest.
    fun finalizeFixedCore(buffer: ByteArray, bufferPos: Int): ByteArray {
        val bs = blockSize.toULong()
        val bitLen: ULong = 8u * (bufferPos.toULong() + bs * blockLen)
        val h = this.h.copyOf()
        len64PaddingBe(buffer, bufferPos, bitLen) { block -> compress(h, arrayOf(block)) }
        val out = ByteArray(SHA1_OUTPUT_SIZE)
        for ((i, v) in h.withIndex()) {
            val base = i * 4
            out[base] = (v shr 24).toByte()
            out[base + 1] = (v shr 16).toByte()
            out[base + 2] = (v shr 8).toByte()
            out[base + 3] = v.toByte()
        }
        return out
    }

    fun reset() {
        SHA1_INITIAL_STATE.copyInto(h)
        blockLen = 0u
    }

    fun copy(): Sha1Core {
        val c = Sha1Core()
        h.copyInto(c.h)
        c.blockLen = blockLen
        return c
    }

    fun writeAlgName(): String = "Sha1"

    fun fmt(): String = "Sha1Core { ... }"

    override fun toString(): String = fmt()

    companion object {
        fun default(): Sha1Core = Sha1Core()
    }
}

/**
 * SHA-1 hasher state.
 *
 * SHA-1 is retained for legacy interoperability only. New security-sensitive
 * code should use a stronger hash. Instances support streaming updates,
 * one-shot hashing through [digest], explicit reset, and finalize-and-reset
 * reuse through [finalizeReset].
 *
 * Example:
 *
 * ```
 * val hasher = Sha1.new()
 * hasher.update("hello world".encodeToByteArray())
 * val output = hasher.finalize()
 * ```
 */
class Sha1 private constructor(
    private val core: Sha1Core,
) {
    private val buffer: ByteArray = ByteArray(BLOCK_SIZE)
    private var bufferPos: Int = 0

    constructor() : this(Sha1Core())

    // Update the hash state with the given input bytes.
    fun update(data: ByteArray) {
        var offset = 0
        var remaining = data.size
        if (bufferPos > 0) {
            val take = minOf(remaining, BLOCK_SIZE - bufferPos)
            data.copyInto(buffer, bufferPos, offset, offset + take)
            bufferPos += take
            offset += take
            remaining -= take
            if (bufferPos == BLOCK_SIZE) {
                core.updateBlocks(arrayOf(buffer.copyOf()))
                bufferPos = 0
            }
        }
        if (remaining >= BLOCK_SIZE) {
            val whole = remaining / BLOCK_SIZE
            val blocks =
                Array(whole) { i ->
                    data.copyOfRange(offset + i * BLOCK_SIZE, offset + (i + 1) * BLOCK_SIZE)
                }
            core.updateBlocks(blocks)
            val consumed = whole * BLOCK_SIZE
            offset += consumed
            remaining -= consumed
        }
        if (remaining > 0) {
            data.copyInto(buffer, 0, offset, offset + remaining)
            bufferPos = remaining
        }
    }

    // Acquire the hash digest.
    fun finalize(): ByteArray = core.finalizeFixedCore(buffer, bufferPos)

    // Finalize and reset for instance reuse.
    fun finalizeReset(): ByteArray {
        val out = core.finalizeFixedCore(buffer, bufferPos)
        core.reset()
        buffer.fill(0)
        bufferPos = 0
        return out
    }

    fun reset() {
        core.reset()
        buffer.fill(0)
        bufferPos = 0
    }

    val blockSize: Int get() = BLOCK_SIZE
    val outputSize: Int get() = SHA1_OUTPUT_SIZE

    companion object {
        fun new(): Sha1 = Sha1()

        // One-shot convenience: hash data in one call and return the digest.
        fun digest(data: ByteArray): ByteArray {
            val h = Sha1()
            h.update(data)
            return h.finalize()
        }
    }
}

// Apply Merkle-Damgård length padding in big-endian form and emit one or two
// completed 64-byte blocks through the supplied compressor.
private inline fun len64PaddingBe(
    buffer: ByteArray,
    bufferPos: Int,
    bitLen: ULong,
    compressor: (ByteArray) -> Unit,
) {
    val block = ByteArray(BLOCK_SIZE)
    buffer.copyInto(block, 0, 0, bufferPos)
    block[bufferPos] = 0x80.toByte()
    if (bufferPos < BLOCK_SIZE - 8) {
        for (i in 0 until 8) {
            block[BLOCK_SIZE - 1 - i] = ((bitLen shr (i * 8)) and 0xFFu).toByte()
        }
        compressor(block)
    } else {
        compressor(block)
        val tail = ByteArray(BLOCK_SIZE)
        for (i in 0 until 8) {
            tail[BLOCK_SIZE - 1 - i] = ((bitLen shr (i * 8)) and 0xFFu).toByte()
        }
        compressor(tail)
    }
}
