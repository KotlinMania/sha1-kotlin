// port-lint: source lib.rs
@file:OptIn(ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.sha1

import io.github.kotlinmania.sha1.compress.BLOCK_SIZE
import io.github.kotlinmania.sha1.compress.compress
import io.github.kotlinmania.sha1.digest.HashMarker
import io.github.kotlinmania.sha1.digest.Output
import io.github.kotlinmania.sha1.digest.core_api.AlgorithmName
import io.github.kotlinmania.sha1.digest.core_api.Block
import io.github.kotlinmania.sha1.digest.core_api.BlockSizeUser
import io.github.kotlinmania.sha1.digest.core_api.Buffer
import io.github.kotlinmania.sha1.digest.core_api.BufferKindUser
import io.github.kotlinmania.sha1.digest.core_api.FixedOutputCore
import io.github.kotlinmania.sha1.digest.core_api.OutputSizeUser
import io.github.kotlinmania.sha1.digest.core_api.Reset
import io.github.kotlinmania.sha1.digest.core_api.UpdateCore

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
    UpdateCore<Sha1Core>,
    FixedOutputCore<Sha1Core>,
    Reset,
    AlgorithmName {
    companion object {
        fun default(): Sha1Core = Sha1Core()
    }

    internal val h: UIntArray = SHA1_INITIAL_STATE.copyOf()
    internal var blockLen: ULong = 0u

    override val blockSize: Int = BLOCK_SIZE
    override val outputSize: Int = SHA1_OUTPUT_SIZE
    override val bufferKind: Any? = io.github.kotlinmania.sha1.digest.block_buffer.Eager

    /**
     * Equivalent to `UpdateCore::updateBlocks`.
     */
    override fun updateBlocks(blocks: Array<Block<Sha1Core>>) {
        blockLen += blocks.size.toULong()
        compress(h, blocks)
    }

    /**
     * Equivalent to `FixedOutputCore::finalize_fixed_core`.
     */
    override fun finalizeFixedCore(buffer: Buffer<Sha1Core>, out: Output<Sha1Core>) {
        val bs = blockSize.toULong()
        val bitLen: ULong = 8u * (buffer.getPos().toULong() + bs * blockLen)
        val h = this.h.copyOf()
        buffer.len64PaddingBe(bitLen) { block -> compress(h, arrayOf(block)) }
        val chunks = out.chunksExactMut(4)
        for ((index, value) in h.withIndex()) {
            val chunkBytes =
                byteArrayOf(
                    (value shr 24).toByte(),
                    (value shr 16).toByte(),
                    (value shr 8).toByte(),
                    value.toByte(),
                )
            chunks[index].copyFrom(chunkBytes)
        }
    }

    override fun reset() {
        SHA1_INITIAL_STATE.copyInto(h)
        blockLen = 0u
    }

    override fun writeAlgName(formatter: StringBuilder) {
        formatter.append("Sha1")
    }

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
) {
    private val buffer: ByteArray = ByteArray(BLOCK_SIZE)
    private var bufferPos: Int = 0

    constructor() : this(Sha1Core())

    /**
     * Adds bytes to the hash stream.
     */
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
    fun finalize(): ByteArray {
        val out = Output<Sha1Core>(SHA1_OUTPUT_SIZE)
        val snapshotBuffer = Buffer<Sha1Core>(buffer.copyOf(), bufferPos)
        core.finalizeFixedCore(snapshotBuffer, out)
        return out.asByteArray()
    }

    /**
     * Finalizes and resets internal state.
     */
    fun finalizeReset(): ByteArray {
        val out = finalize()
        reset()
        return out
    }

    /**
     * Resets internal state, including partial buffered bytes.
     */
    fun reset() {
        core.reset()
        buffer.fill(0)
        bufferPos = 0
    }

    val blockSize: Int get() = BLOCK_SIZE
    val outputSize: Int get() = SHA1_OUTPUT_SIZE

    companion object {
        fun new(): Sha1 = Sha1()

        fun digest(data: ByteArray): ByteArray {
            val h = Sha1()
            h.update(data)
            return h.finalize()
        }
    }
}
