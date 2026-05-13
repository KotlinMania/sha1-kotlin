// port-lint: source src/compress.rs
@file:OptIn(ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.sha1.compress

// Upstream picks an architecture-specific compression backend at compile time:
// a forced software build, ARM64 with assembly, LOONGARCH64 with assembly, or
// x86/x86_64 with optional assembly each dispatch to the matching submodule;
// every other target falls through to the software path. Kotlin Multiplatform
// exposes no equivalent inline assembly across all KMP targets, so this port
// keeps only the software implementation.

internal const val BLOCK_SIZE: Int = 64

/** SHA-1 compression function */
internal fun compress(state: UIntArray, blocks: Array<ByteArray>) {
    // Safety: the upstream reinterprets a 64-byte block container as a fixed
    // 64-byte array via an unchecked cast. The Kotlin port already operates on
    // ByteArray blocks, so no reinterpretation is required.
    compressSoft(state, blocks)
}
