@file:OptIn(ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.sha1.compress

import io.github.kotlinmania.sha1.compress.soft.compressSoft

internal actual fun platformCompress(state: UIntArray, blocks: Array<ByteArray>) {
    compressSoft(state, blocks)
}
