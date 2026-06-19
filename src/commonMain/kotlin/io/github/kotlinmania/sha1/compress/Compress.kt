// port-lint: source compress.rs
@file:OptIn(ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.sha1.compress

// The original crate can select hardware-specific compression backends.
// Kotlin Multiplatform needs one portable implementation across the configured
// targets, so the shared dispatcher routes through the software backend.

internal const val BLOCK_SIZE: Int = 64

/** SHA-1 compression function */
internal fun compress(state: UIntArray, blocks: Array<ByteArray>) {
    SoftBackend.compress(state, blocks)
}
