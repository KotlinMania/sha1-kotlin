// port-lint: source compress/aarch64.rs
@file:OptIn(ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.sha1.compress

internal object Aarch64Backend {
    fun compress(state: UIntArray, blocks: Array<ByteArray>) {
        SoftBackend.compress(state, blocks)
    }
}
