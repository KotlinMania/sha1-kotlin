// port-lint: tests benches/lib.rs
@file:OptIn(ExperimentalForeignApi::class, ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.sha1.compress

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import sha1_asm.sha1_asm_compress_blocks
import kotlin.test.Test
import kotlin.test.assertContentEquals

class Sha1AsmNativeTest {
    private fun initialState(): UIntArray =
        uintArrayOf(0x67452301u, 0xEFCDAB89u, 0x98BADCFEu, 0x10325476u, 0xC3D2E1F0u)

    private fun padSingleBlock(message: ByteArray): ByteArray {
        require(message.size < 56) { "single-block helper assumes message fits in one padded block" }
        val block = ByteArray(BLOCK_SIZE)
        message.copyInto(block)
        block[message.size] = 0x80.toByte()
        val bitLen = message.size.toLong() * 8L
        for (i in 0 until 8) {
            block[BLOCK_SIZE - 1 - i] = ((bitLen ushr (i * 8)) and 0xFFL).toByte()
        }
        return block
    }

    private fun nativeCompress(initial: UIntArray, blocks: Array<ByteArray>): UIntArray {
        val state = initial.copyOf()
        val blockBytes = UByteArray(blocks.size * BLOCK_SIZE)
        for ((blockIndex, block) in blocks.withIndex()) {
            require(block.size == BLOCK_SIZE) { "SHA-1 compression blocks must be exactly 64 bytes" }
            val destinationOffset = blockIndex * BLOCK_SIZE
            for (i in block.indices) {
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
        return state
    }

    @Test
    fun upstreamBenchZeroBlockUpdatesZeroState() {
        val state =
            nativeCompress(
                UIntArray(5),
                arrayOf(ByteArray(BLOCK_SIZE)),
            )

        assertContentEquals(
            uintArrayOf(0x9E1547EDu, 0x57EC91C2u, 0x30FA8BC8u, 0xC7785A54u, 0xA7EFA5E3u),
            state,
        )
    }

    @Test
    fun emptyStringPaddedBlockMatchesKnownDigest() {
        val state = nativeCompress(initialState(), arrayOf(padSingleBlock(ByteArray(0))))

        assertContentEquals(
            uintArrayOf(0xDA39A3EEu, 0x5E6B4B0Du, 0x3255BFEFu, 0x95601890u, 0xAFD80709u),
            state,
        )
    }

    @Test
    fun abcPaddedBlockMatchesKnownDigest() {
        val state = nativeCompress(initialState(), arrayOf(padSingleBlock(byteArrayOf(0x61, 0x62, 0x63))))

        assertContentEquals(
            uintArrayOf(0xA9993E36u, 0x4706816Au, 0xBA3E2571u, 0x7850C26Cu, 0x9CD0D89Du),
            state,
        )
    }

    @Test
    fun multipleBlocksAreCompressedInOrder() {
        val state =
            nativeCompress(
                initialState(),
                arrayOf(padSingleBlock(byteArrayOf(0x61, 0x62, 0x63)), ByteArray(BLOCK_SIZE)),
            )

        assertContentEquals(
            uintArrayOf(0xE138DC24u, 0x275DCCF0u, 0x40E8DFF7u, 0x0E2E258Eu, 0xAA13BE7Du),
            state,
        )
    }
}
