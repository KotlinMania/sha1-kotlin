// port-lint: source compress/aarch64.rs
package io.github.kotlinmania.sha1.compress.aarch64

import io.github.kotlinmania.sha1.compress.soft.compressSoft

/**
 * Kotlin shim for the upstream `aarch64` backend.
 *
 * The Rust implementation uses a native backend when the `sha2` target feature
 * indicates SHA support; otherwise it falls back to software.
 */
internal fun compress(state: UIntArray, blocks: Array<ByteArray>) {
    compressSoft(state, blocks)
}
