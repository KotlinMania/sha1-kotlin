// port-lint: source compress.rs
@file:OptIn(ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.sha1.compress

import io.github.kotlinmania.sha1.compress.soft.compressSoft

/**
 * SHA-1 compression function selector.
 *
 * The upstream crate resolves an architecture-specific backend at build time:
 * software fallback, aarch64 with native instructions, loongarch64 assembly
 * backend, or x86 with optional SHA extensions.
 */

internal const val BLOCK_SIZE: Int = 64

/**
 * Applies the SHA-1 block compression function to a sequence of 64-byte blocks.
 *
 * The upstream implementation reinterprets compressed blocks between
 * contiguous array views before calling the selected backend implementation.
 */
internal fun compress(state: UIntArray, blocks: Array<ByteArray>) {
    // The port uses `ByteArray` blocks directly, so no unchecked cast is needed.
    compressSoft(state, blocks)
}
