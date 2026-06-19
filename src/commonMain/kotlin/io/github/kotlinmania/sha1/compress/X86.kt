// port-lint: source compress/x86.rs
@file:OptIn(ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.sha1.compress

internal object X86Backend {
    fun digestBlocks(state: UIntArray, blocks: Array<ByteArray>) {
        SoftBackend.compress(state, blocks)
    }

    fun compress(state: UIntArray, blocks: Array<ByteArray>) {
        digestBlocks(state, blocks)
    }
}
