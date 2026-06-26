package io.github.kotlinmania.sha1.digest.core_api

/**
 * Kotlin-side equivalent of `digest::core_api::Buffer`.
 *
 * Only the operations needed by this crate are implemented here.
 */
class Buffer<T>(private val bytes: ByteArray, private val pos: Int) {
    fun getPos(): Int = pos
    fun get_pos(): Int = pos

    fun len64PaddingBe(bitLen: ULong, compressor: (ByteArray) -> Unit) {
        val blockSize = bytes.size
        val block = ByteArray(blockSize)
        bytes.copyInto(block, 0, 0, pos)
        block[pos] = 0x80.toByte()
        if (pos < blockSize - 8) {
            for (i in 0 until 8) {
                block[blockSize - 1 - i] = ((bitLen shr (i * 8)) and 0xFFu).toByte()
            }
            compressor(block)
        } else {
            compressor(block)
            val tail = ByteArray(blockSize)
            for (i in 0 until 8) {
                tail[blockSize - 1 - i] = ((bitLen shr (i * 8)) and 0xFFu).toByte()
            }
            compressor(tail)
        }
    }
}
