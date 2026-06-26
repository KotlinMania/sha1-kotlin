// port-lint: source lib.rs
@file:OptIn(ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.sha1

import io.github.kotlinmania.digest.AlgorithmName
import io.github.kotlinmania.digest.Block
import io.github.kotlinmania.digest.BlockSizeUser
import io.github.kotlinmania.digest.BufferKind
import io.github.kotlinmania.digest.BufferKindUser
import io.github.kotlinmania.digest.Digest
import io.github.kotlinmania.digest.DigestFactory
import io.github.kotlinmania.digest.Eager
import io.github.kotlinmania.digest.HashMarker
import io.github.kotlinmania.digest.Output
import io.github.kotlinmania.digest.OutputSizeUser
import io.github.kotlinmania.digest.Reset
import io.github.kotlinmania.digest.UpdateCore
import io.github.kotlinmania.digest.fmt.Formatter
import io.github.kotlinmania.sha1.compress.BLOCK_SIZE
import io.github.kotlinmania.sha1.compress.compress

/**
 * Core hash state for SHA-1.
 */
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

/**
 * Core SHA-1 hasher state.
 */
internal class Sha1Core :
    HashMarker,
    BlockSizeUser,
    BufferKindUser,
    OutputSizeUser,
    UpdateCore,
    Reset,
    AlgorithmName {
    companion object {
        fun default(): Sha1Core = Sha1Core()
    }

    internal val h: UIntArray = SHA1_INITIAL_STATE.copyOf()
    internal var blockLen: ULong = 0u

    override val blockSize: Int = BLOCK_SIZE
    override val outputSize: Int = SHA1_OUTPUT_SIZE
    override val bufferKind: BufferKind = Eager

    override fun updateBlocks(blocks: List<Block<*>>) {
        blockLen += blocks.size.toULong()
        compress(h, blocks.toTypedArray())
    }

    fun updateBlocks(blocks: Array<ByteArray>) {
        updateBlocks(blocks.asList())
    }

    internal fun finalizeWithPadding(pending: ByteArray, pendingLength: Int, out: Output<*>) {
        require(pendingLength in 0 until blockSize) { "pending SHA-1 block length must be less than one block" }
        val bs = blockSize.toULong()
        val bitLen: ULong = 8u * (pendingLength.toULong() + bs * blockLen)
        val h = this.h.copyOf()
        len64PaddingBe(pending, pendingLength, bitLen) { block -> compress(h, arrayOf(block)) }
        writeDigestWords(h, out)
    }

    private fun len64PaddingBe(pending: ByteArray, pendingLength: Int, bitLen: ULong, compressor: (ByteArray) -> Unit) {
        val block = ByteArray(blockSize)
        pending.copyInto(block, 0, 0, pendingLength)
        block[pendingLength] = 0x80.toByte()
        if (pendingLength < blockSize - 8) {
            writeLength(block, bitLen)
            compressor(block)
        } else {
            compressor(block)
            val tail = ByteArray(blockSize)
            writeLength(tail, bitLen)
            compressor(tail)
        }
    }

    private fun writeLength(block: ByteArray, bitLen: ULong) {
        for (i in 0 until 8) {
            block[blockSize - 1 - i] = ((bitLen shr (i * 8)) and 0xFFu).toByte()
        }
    }

    private fun writeDigestWords(words: UIntArray, out: Output<*>) {
        require(out.size >= SHA1_OUTPUT_SIZE) { "SHA-1 output buffer must be at least 20 bytes" }
        for ((index, value) in words.withIndex()) {
            val offset = index * 4
            out[offset] = (value shr 24).toByte()
            out[offset + 1] = (value shr 16).toByte()
            out[offset + 2] = (value shr 8).toByte()
            out[offset + 3] = value.toByte()
        }
    }

    override fun reset() {
        SHA1_INITIAL_STATE.copyInto(h)
        blockLen = 0u
    }

    override fun writeAlgName(formatter: Formatter): Result<Unit> = formatter.writeString("Sha1")

    fun writeAlgName(): String = "Sha1"

    fun copy(): Sha1Core {
        val c = Sha1Core()
        h.copyInto(c.h)
        c.blockLen = blockLen
        return c
    }

    override fun toString(): String = "Sha1Core { ... }"

    fun fmt(formatter: StringBuilder) {
        formatter.append("Sha1Core { ... }")
    }
}

/**
 * Public SHA-1 hasher state.
 */
class Sha1 private constructor(
    private val core: Sha1Core,
) : Digest,
    BlockSizeUser {
    private val buffer: ByteArray = ByteArray(BLOCK_SIZE)
    private var bufferPos: Int = 0

    constructor() : this(Sha1Core())

    /**
     * Adds bytes to the hash stream.
     */
    override fun update(data: ByteArray) {
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
                Array(whole) { index ->
                    data.copyOfRange(offset + index * BLOCK_SIZE, offset + (index + 1) * BLOCK_SIZE)
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

    /**
     * Finalizes and returns the digest bytes.
     */
    override fun finalize(): ByteArray = finalizeFixed()

    /**
     * Finalizes and resets internal state.
     */
    override fun finalizeReset(): ByteArray = finalizeFixedReset()

    override fun finalizeInto(out: Output<*>) {
        core.copy().finalizeWithPadding(buffer, bufferPos, out)
    }

    override fun finalizeIntoReset(out: Output<*>) {
        finalizeInto(out)
        reset()
    }

    /**
     * Resets internal state, including partial buffered bytes.
     */
    override fun reset() {
        core.reset()
        buffer.fill(0)
        bufferPos = 0
    }

    override val blockSize: Int get() = BLOCK_SIZE
    override val outputSize: Int get() = SHA1_OUTPUT_SIZE

    companion object {
        init {
            Digest.register(
                Sha1::class,
                object : DigestFactory<Sha1> {
                    override fun new(): Sha1 = Sha1()

                    override val outputSize: Int = SHA1_OUTPUT_SIZE
                    override val blockSize: Int = BLOCK_SIZE
                },
            )
        }

        fun new(): Sha1 = Sha1()

        fun digest(data: ByteArray): ByteArray {
            val h = Sha1()
            h.update(data)
            return h.finalize()
        }
    }
}
