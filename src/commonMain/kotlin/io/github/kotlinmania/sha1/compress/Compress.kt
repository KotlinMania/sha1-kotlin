// port-lint: source compress.rs
@file:OptIn(ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.sha1.compress

internal const val BLOCK_SIZE: Int = 64

/**
 * Platform-specific SHA-1 block compression dispatcher.
 */
internal expect fun platformCompress(state: UIntArray, blocks: Array<ByteArray>)

/**
 * Applies the SHA-1 block compression function to a sequence of 64-byte blocks.
 *
 * The upstream implementation reinterprets compressed blocks between
 * contiguous array views before calling the selected backend implementation.
 */
fun compress(state: UIntArray, blocks: Array<ByteArray>) {
    platformCompress(state, blocks)
}
