// port-lint: source src/lib.rs
@file:OptIn(ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.sha1

import io.github.kotlinmania.sha1.compress.BLOCK_SIZE
import io.github.kotlinmania.sha1.compress.compress

// Pure Kotlin implementation of the SHA-1 cryptographic hash algorithm
// (https://en.wikipedia.org/wiki/SHA-1).
//
// SHA-1 should be considered cryptographically broken and unsuitable for
// further use in any security critical capacity, as it is practically
// vulnerable to chosen-prefix collisions
// (https://sha-mbles.github.io/). This module exists for legacy
// interoperability purposes only.
//
// Usage:
//
//     val hasher = Sha1()
//     hasher.update("hello world".encodeToByteArray())
//     val result = hasher.finalize()
//     // result content equals 2aae6c35c94fcfb415dbe95f408b9ce91ee846ed
//
// Upstream Rust re-exports the digest crate's Digest convenience trait
// through pub use digest::{self, Digest}; the Kotlin port exposes the
// equivalent operations directly on the Sha1 class below, since the
// digest framework abstractions are translated separately under the
// io.github.kotlinmania.digest package.

internal const val STATE_LEN: Int = 5

internal val SHA1_INITIAL_STATE: UIntArray = uintArrayOf(
    0x67452301u,
    0xEFCDAB89u,
    0x98BADCFEu,
    0x10325476u,
    0xC3D2E1F0u,
)

internal const val SHA1_OUTPUT_SIZE: Int = 20

// Core SHA-1 hasher state. Matches Sha1Core in upstream: holds the running
// state h plus the count of compressed blocks. Upstream uses an Eager
// block-buffer kind, modelled here by Sha1 below; Sha1Core itself only
// consumes whole blocks.
class Sha1Core {
    internal val h: UIntArray = SHA1_INITIAL_STATE.copyOf()
    internal var blockLen: ULong = 0u

    internal val blockSize: Int get() = BLOCK_SIZE
    internal val outputSize: Int get() = SHA1_OUTPUT_SIZE

    // BlockSizeUser, BufferKindUser, OutputSizeUser, HashMarker are marker
    // associations on the Rust side; in Kotlin they collapse into the
    // fixed blockSize/outputSize values above.

    // UpdateCore: consume whole blocks. The caller is responsible for
    // assembling 64-byte blocks; this matches the Rust signature
    // update_blocks(&mut self, blocks: &[Block<Self>]).
    fun updateBlocks(blocks: Array<ByteArray>) {
        blockLen += blocks.size.toULong()
        compress(h, blocks)
    }

    // FixedOutputCore: apply MD-style padding and emit the final 20-byte
    // digest. Upstream delegates the padding work to digest::Buffer's
    // len64_padding_be helper; that helper is inlined here so this file
    // does not require the digest-kotlin sibling. bufferPos is the number
    // of bytes currently held in the caller's block buffer (0..63), and
    // buffer holds those bytes in its first bufferPos slots.
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

    // Reset: equivalent of impl Reset where *self = Default::default().
    fun reset() {
        SHA1_INITIAL_STATE.copyInto(h)
        blockLen = 0u
    }

    // Clone semantics: Sha1Core derives Clone in upstream. Kotlin lacks
    // automatic Clone; provide an explicit copy that produces an
    // independent state.
    fun copy(): Sha1Core {
        val c = Sha1Core()
        h.copyInto(c.h)
        c.blockLen = blockLen
        return c
    }

    // AlgorithmName: write_alg_name writes the literal "Sha1".
    fun writeAlgName(): String = "Sha1"

    // The Rust Debug impl prints "Sha1Core { ... }".
    override fun toString(): String = "Sha1Core { ... }"
}

// SHA-1 hasher state. Upstream defines this as
// pub type Sha1 = CoreWrapper<Sha1Core>; the wrapper from the digest crate
// adds a 64-byte Eager block buffer plus convenience update/finalize/reset
// methods. Kotlin does not have Rust type aliases over generic structs
// with attached methods, so the wrapper is materialized as a thin class
// holding a Sha1Core plus its own block buffer.
class Sha1 private constructor(private val core: Sha1Core) {
    private val buffer: ByteArray = ByteArray(BLOCK_SIZE)
    private var bufferPos: Int = 0

    constructor() : this(Sha1Core())

    // Update the hash state with the given input bytes. Equivalent to
    // Digest::update on the upstream CoreWrapper.
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
            val blocks = Array(whole) { i ->
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

    // Acquire the hash digest. Consumes the hasher in upstream
    // (CoreWrapper::finalize takes self by value); Kotlin returns the
    // digest and leaves the hasher in a finalized state. Call reset to
    // re-use the instance, mirroring finalize_reset.
    fun finalize(): ByteArray = core.finalizeFixedCore(buffer, bufferPos)

    // Finalize and reset, matching CoreWrapper's finalize_reset path.
    fun finalizeReset(): ByteArray {
        val out = core.finalizeFixedCore(buffer, bufferPos)
        core.reset()
        buffer.fill(0)
        bufferPos = 0
        return out
    }

    // Reset the hasher to its initial state.
    fun reset() {
        core.reset()
        buffer.fill(0)
        bufferPos = 0
    }

    val blockSize: Int get() = BLOCK_SIZE
    val outputSize: Int get() = SHA1_OUTPUT_SIZE

    companion object {
        // Equivalent of Digest::new — create a fresh hasher in its
        // initial state.
        fun new(): Sha1 = Sha1()

        // One-shot convenience matching the Rust Digest::digest helper:
        // hash data in one call and return the 20-byte output.
        fun digest(data: ByteArray): ByteArray {
            val h = Sha1()
            h.update(data)
            return h.finalize()
        }
    }
}

// Apply Merkle-Damgård length-64 padding in big-endian form. Equivalent to
// digest::block_buffer::Buffer::len64_padding_be on the Rust side: write
// 0x80 immediately after the buffered bytes, zero-pad until the trailing
// 8 bytes of the final block, then write the 64-bit big-endian bit length
// and emit one or two completed 64-byte blocks through the supplied
// compressor.
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
        // Length fits in the trailing 8 bytes of this same block.
        for (i in 0 until 8) {
            block[BLOCK_SIZE - 1 - i] = ((bitLen shr (i * 8)) and 0xFFu).toByte()
        }
        compressor(block)
    } else {
        // Need a second block. Emit the current one with only the 0x80
        // marker plus zero padding, then a fresh block carrying the
        // 64-bit length tail.
        compressor(block)
        val tail = ByteArray(BLOCK_SIZE)
        for (i in 0 until 8) {
            tail[BLOCK_SIZE - 1 - i] = ((bitLen shr (i * 8)) and 0xFFu).toByte()
        }
        compressor(tail)
    }
}
