// port-lint: source compress/x86.rs
package io.github.kotlinmania.sha1.compress.x86

import io.github.kotlinmania.sha1.compress.soft.compressSoft

/**
 * Kotlin-shim for the upstream `x86` backend.
 *
 * The Rust source dispatches to SHA-NI intrinsics when `shani cpuid`
 * is available and falls back to software when not. This Kotlin port keeps
 * a portable path only, while preserving symbol compatibility.
 */
private val hasShaExtensions: Boolean = false

/**
 * Fallback dispatch hook.
 *
 * Upstream checks CPU capabilities here; Kotlin keeps this branch explicit
 * to preserve behavior while remaining platform-independent.
 */
internal fun compress(state: UIntArray, blocks: Array<ByteArray>) {
    if (hasShaExtensions) {
        digestBlocks(state, blocks)
    } else {
        compressSoft(state, blocks)
    }
}

private fun digestBlocks(state: UIntArray, blocks: Array<ByteArray>) {
    compressSoft(state, blocks)
    // Kept for parity with upstream x86 symbol surface.
    // Kotlin does not expose direct SHA-NI intrinsics here.
}
