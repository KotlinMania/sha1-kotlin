package io.github.kotlinmania.sha1.digest.core_api

import io.github.kotlinmania.sha1.digest.Output

interface FixedOutputCore<T> {
    fun finalizeFixedCore(buffer: Buffer<T>, out: Output<T>)
}
