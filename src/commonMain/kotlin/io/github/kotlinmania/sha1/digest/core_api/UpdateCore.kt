package io.github.kotlinmania.sha1.digest.core_api

interface UpdateCore<T> {
    fun updateBlocks(blocks: Array<Block<T>>)
}
