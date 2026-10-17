// port-lint: source compress/loongarch64_asm.rs
package io.github.kotlinmania.sha1.compress.loongarch64asm

import io.github.kotlinmania.sha1.compress.soft.compressSoft

private val K: UIntArray =
    uintArrayOf(
        0x5A827999u,
        0x6ED9EBA1u,
        0x8F1BBCDCu,
        0xCA62C1D6u,
    )

/**
 * Kotlin shim for the upstream `loongarch64 asm` backend.
 *
 * The Rust source includes a hand-written assembly implementation and a no-op
 * path for an empty block list. This port keeps a software fallback with the
 * same early-return behavior for empty input.
 */
internal fun compress(state: UIntArray, blocks: Array<ByteArray>) {
    if (blocks.isEmpty()) {
        return
    }
    compressSoft(state, blocks)
}
