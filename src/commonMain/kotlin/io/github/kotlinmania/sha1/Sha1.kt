package io.github.kotlinmania.sha1

import io.github.kotlinmania.digest.BlockSizeUser
import io.github.kotlinmania.digest.Digest
import io.github.kotlinmania.digest.DigestFactory
import io.github.kotlinmania.digest.Output
import io.github.kotlinmania.digest.coreapi.CoreWrapper
import io.github.kotlinmania.sha1.compress.BLOCK_SIZE
import io.github.kotlinmania.zeroize.Zeroize

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
    core: Sha1Core,
) : Digest,
    BlockSizeUser,
    Zeroize {
    private val wrapper: CoreWrapper<Sha1Core> = CoreWrapper(core)

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

    override fun zeroize() {
        wrapper.reset()
    }

    fun copy(): Sha1 {
        val (innerCore, buffer) = wrapper.decompose()
        val copy = Sha1(innerCore.copy())
        val remaining = buffer.remainingBytes()
        if (remaining.isNotEmpty()) {
            copy.update(remaining)
        }
        return copy
    }

    override val blockSize: Int get() = BLOCK_SIZE
    override val outputSize: Int get() = SHA1_OUTPUT_SIZE

    override fun toString(): String = wrapper.toString()

    companion object {
        const val OID: String = Sha1Core.OID

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

        fun newWithPrefix(data: ByteArray): Sha1 {
            val h = Sha1()
            h.update(data)
            return h
        }

        fun digest(data: ByteArray): ByteArray {
            val h = Sha1()
            h.update(data)
            return h.finalize()
        }
    }
}
