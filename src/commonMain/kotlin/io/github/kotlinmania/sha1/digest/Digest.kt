// Minimal trait surface to mirror the upstream `digest` crate API pieces
// used by this transliteration. This is intentionally compact for `sha1-kotlin`
// while keeping method names stable.
package io.github.kotlinmania.sha1.digest

interface Digest {
    fun update(data: ByteArray)
    fun finalize(): ByteArray
    fun reset()

    val blockSize: Int
    val outputSize: Int
}

// Export-like helper to mirror `pub use digest::{self, Digest}` from upstream.
object digest
