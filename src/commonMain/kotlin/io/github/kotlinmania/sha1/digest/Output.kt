package io.github.kotlinmania.sha1.digest

/**
 * Byte output sink for fixed-size digest outputs.
 */
class Output<T>(private val bytes: ByteArray = ByteArray(0)) {
    constructor(outputSize: Int) : this(ByteArray(outputSize))

    val size: Int
        get() = bytes.size

    fun asByteArray(): ByteArray = bytes

    fun chunksExactMut(chunkSize: Int): Array<Chunk<T>> {
        if (chunkSize <= 0) return emptyArray()
        val chunkCount = bytes.size / chunkSize
        return Array(chunkCount) { index -> Chunk(this, index * chunkSize, chunkSize) }
    }

    class Chunk<T>(private val output: Output<T>, private val start: Int, private val length: Int) {
        fun copyFrom(source: ByteArray) {
            val copyLength = minOf(length, source.size)
            source.copyInto(output.bytes, start, 0, copyLength)
            if (copyLength < length) {
                for (i in copyLength until length) {
                    output.bytes[start + i] = 0
                }
            }
        }
    }
}
