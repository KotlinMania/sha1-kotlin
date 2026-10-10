// port-lint: source compress.rs
@file:OptIn(ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.sha1.compress

import io.github.kotlinmania.sha1.compress.soft.compressSoft

// The original crate can select hardware-specific compression backends.
// Kotlin Multiplatform needs one portable implementation across the configured
// targets, so the shared dispatcher routes through the software backend.

internal const val BLOCK_SIZE: Int = 64

/**
 * Applies the SHA-1 block compression function to a sequence of 64-byte blocks.
 *
 * The upstream implementation reinterprets compressed blocks between
 * contiguous array views before calling the selected backend implementation.
 */
internal fun compress(state: UIntArray, blocks: Array<ByteArray>) {
    compressSoft(state, blocks)
}
