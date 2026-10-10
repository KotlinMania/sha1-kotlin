// port-lint: source lib.rs
@file:OptIn(ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.sha1

import io.github.kotlinmania.digest.Block
import io.github.kotlinmania.digest.BlockSizeUser
import io.github.kotlinmania.digest.Digest
import io.github.kotlinmania.digest.DigestFactory
import io.github.kotlinmania.digest.HashMarker
import io.github.kotlinmania.digest.Output
import io.github.kotlinmania.digest.OutputSizeUser
import io.github.kotlinmania.digest.Reset
import io.github.kotlinmania.digest.coreapi.AlgorithmName
import io.github.kotlinmania.digest.coreapi.Buffer
import io.github.kotlinmania.digest.coreapi.BufferKind
import io.github.kotlinmania.digest.coreapi.BufferKindUser
import io.github.kotlinmania.digest.coreapi.CoreWrapper
import io.github.kotlinmania.digest.coreapi.Eager
import io.github.kotlinmania.digest.coreapi.FixedOutputCore
import io.github.kotlinmania.digest.coreapi.UpdateCore
import io.github.kotlinmania.digest.fmt.FmtResult
import io.github.kotlinmania.digest.fmt.Formatter
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
    val EAGER: BufferKind = Eager
}

object OutputSize {
    const val BYTES: Int = SHA1_OUTPUT_SIZE
}

/**
 * Core SHA-1 hasher state.
 *
 * The core owns the five running words and the number of complete blocks that
 * have already been compressed. It only accepts full 64-byte blocks; [Sha1]
 * owns the partial block buffer and final padding.
 */
class Sha1Core :
    HashMarker,
    BlockSizeUser,
    BufferKindUser,
    OutputSizeUser,
    UpdateCore,
    FixedOutputCore,
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

    override fun finalizeFixedCore(buffer: Buffer<*>, out: Output<*>) {
        val bs = blockSize.toULong()
        val bitLen: ULong = 8u * (buffer.getPos().toULong() + bs * blockLen)
        val h = this.h.copyOf()
        buffer.len64PaddingBe(bitLen) { block ->
            compress(h, arrayOf(block))
        }
        writeDigestWords(h, out)
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

    override fun writeAlgName(formatter: Formatter): FmtResult = formatter.writeString("Sha1")

    fun writeAlgName(): String = "Sha1"

    fun copy(): Sha1Core {
        val c = Sha1Core()
        h.copyInto(c.h)
        c.blockLen = blockLen
        return c
    }

    fun fmt(): String = "Sha1Core { ... }"

    override fun toString(): String = fmt()
}

/**
 * Public SHA-1 hasher state.
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
) : Digest,
    BlockSizeUser {
    private var wrapper: CoreWrapper<Sha1Core> = CoreWrapper(core)

    constructor() : this(Sha1Core())

    override fun update(data: ByteArray) {
        wrapper.update(data)
    }

    override fun finalize(): ByteArray = wrapper.finalizeFixed()

    override fun finalizeReset(): ByteArray = wrapper.finalizeFixedReset()

    override fun finalizeInto(out: Output<*>) {
        wrapper.finalizeInto(out)
    }

    override fun finalizeIntoReset(out: Output<*>) {
        wrapper.finalizeIntoReset(out)
    }

    override fun reset() {
        wrapper.reset()
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
