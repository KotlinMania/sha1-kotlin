@file:OptIn(ExperimentalForeignApi::class, ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.sha1.compress

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import sha1_asm.sha1_asm_compress_blocks

internal actual fun platformCompress(state: UIntArray, blocks: Array<ByteArray>) {
    require(state.size >= 5) { "SHA-1 state array must contain at least 5 words" }
    if (blocks.isEmpty()) return
    val blockBytes = UByteArray(blocks.size * BLOCK_SIZE)
    for ((blockIndex, block) in blocks.withIndex()) {
        require(block.size == BLOCK_SIZE) { "SHA-1 compression blocks must be exactly 64 bytes" }
        val destinationOffset = blockIndex * BLOCK_SIZE
        for (i in 0 until BLOCK_SIZE) {
            blockBytes[destinationOffset + i] = block[i].toUByte()
        }
    }
    state.usePinned { pinnedState ->
        blockBytes.usePinned { pinnedBlocks ->
            sha1_asm_compress_blocks(
                pinnedState.addressOf(0),
                pinnedBlocks.addressOf(0),
                blocks.size.convert(),
            )
        }
    }
}
