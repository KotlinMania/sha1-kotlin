// port-lint: source compress/loongarch64_asm.rs
@file:OptIn(ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.sha1.compress

internal object Loongarch64AsmBackend {
    fun compress(state: UIntArray, blocks: Array<ByteArray>) {
        if (blocks.isEmpty()) {
            return
        }
        SoftBackend.compress(state, blocks)
    }
}
