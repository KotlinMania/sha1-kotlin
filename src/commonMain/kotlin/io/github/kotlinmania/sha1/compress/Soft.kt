// port-lint: source src/compress/soft.rs
@file:OptIn(ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.sha1.compress

private val K: UIntArray = uintArrayOf(0x5A827999u, 0x6ED9EBA1u, 0x8F1BBCDCu, 0xCA62C1D6u)

private fun add(a: UIntArray, b: UIntArray): UIntArray =
    uintArrayOf(
        a[0] + b[0],
        a[1] + b[1],
        a[2] + b[2],
        a[3] + b[3],
    )

private fun xor(a: UIntArray, b: UIntArray): UIntArray =
    uintArrayOf(a[0] xor b[0], a[1] xor b[1], a[2] xor b[2], a[3] xor b[3])

internal fun sha1FirstAdd(e: UInt, w0: UIntArray): UIntArray {
    val a = w0[0]
    val b = w0[1]
    val c = w0[2]
    val d = w0[3]
    return uintArrayOf(e + a, b, c, d)
}

private fun sha1msg1(a: UIntArray, b: UIntArray): UIntArray {
    val w2 = a[2]
    val w3 = a[3]
    val w4 = b[0]
    val w5 = b[1]
    return uintArrayOf(a[0] xor w2, a[1] xor w3, a[2] xor w4, a[3] xor w5)
}

private fun sha1msg2(a: UIntArray, b: UIntArray): UIntArray {
    val x0 = a[0]
    val x1 = a[1]
    val x2 = a[2]
    val x3 = a[3]
    val w13 = b[1]
    val w14 = b[2]
    val w15 = b[3]

    val w16 = (x0 xor w13).rotateLeft(1)
    val w17 = (x1 xor w14).rotateLeft(1)
    val w18 = (x2 xor w15).rotateLeft(1)
    val w19 = (x3 xor w16).rotateLeft(1)

    return uintArrayOf(w16, w17, w18, w19)
}

private fun sha1FirstHalf(abcd: UIntArray, msg: UIntArray): UIntArray =
    sha1FirstAdd(abcd[0].rotateLeft(30), msg)

private fun sha1DigestRoundX4(abcd: UIntArray, work: UIntArray, i: Int): UIntArray =
    when (i) {
        0 -> sha1rnds4c(abcd, add(work, uintArrayOf(K[0], K[0], K[0], K[0])))
        1 -> sha1rnds4p(abcd, add(work, uintArrayOf(K[1], K[1], K[1], K[1])))
        2 -> sha1rnds4m(abcd, add(work, uintArrayOf(K[2], K[2], K[2], K[2])))
        3 -> sha1rnds4p(abcd, add(work, uintArrayOf(K[3], K[3], K[3], K[3])))
        else -> error("unknown icosaround index")
    }

private fun sha1rnds4c(abcd: UIntArray, msg: UIntArray): UIntArray {
    var a = abcd[0]
    var b = abcd[1]
    var c = abcd[2]
    var d = abcd[3]
    val t = msg[0]
    val u = msg[1]
    val v = msg[2]
    val w = msg[3]
    var e = 0u

    // Choose, MD5F, SHA1C
    fun bool3ary202(x: UInt, y: UInt, z: UInt): UInt = z xor (x and (y xor z))

    e = e + a.rotateLeft(5) + bool3ary202(b, c, d) + t
    b = b.rotateLeft(30)

    d = d + e.rotateLeft(5) + bool3ary202(a, b, c) + u
    a = a.rotateLeft(30)

    c = c + d.rotateLeft(5) + bool3ary202(e, a, b) + v
    e = e.rotateLeft(30)

    b = b + c.rotateLeft(5) + bool3ary202(d, e, a) + w
    d = d.rotateLeft(30)

    return uintArrayOf(b, c, d, e)
}

private fun sha1rnds4p(abcd: UIntArray, msg: UIntArray): UIntArray {
    var a = abcd[0]
    var b = abcd[1]
    var c = abcd[2]
    var d = abcd[3]
    val t = msg[0]
    val u = msg[1]
    val v = msg[2]
    val w = msg[3]
    var e = 0u

    // Parity, XOR, MD5H, SHA1P
    fun bool3ary150(x: UInt, y: UInt, z: UInt): UInt = x xor y xor z

    e = e + a.rotateLeft(5) + bool3ary150(b, c, d) + t
    b = b.rotateLeft(30)

    d = d + e.rotateLeft(5) + bool3ary150(a, b, c) + u
    a = a.rotateLeft(30)

    c = c + d.rotateLeft(5) + bool3ary150(e, a, b) + v
    e = e.rotateLeft(30)

    b = b + c.rotateLeft(5) + bool3ary150(d, e, a) + w
    d = d.rotateLeft(30)

    return uintArrayOf(b, c, d, e)
}

private fun sha1rnds4m(abcd: UIntArray, msg: UIntArray): UIntArray {
    var a = abcd[0]
    var b = abcd[1]
    var c = abcd[2]
    var d = abcd[3]
    val t = msg[0]
    val u = msg[1]
    val v = msg[2]
    val w = msg[3]
    var e = 0u

    // Majority, SHA1M
    fun bool3ary232(x: UInt, y: UInt, z: UInt): UInt = (x and y) xor (x and z) xor (y and z)

    e = e + a.rotateLeft(5) + bool3ary232(b, c, d) + t
    b = b.rotateLeft(30)

    d = d + e.rotateLeft(5) + bool3ary232(a, b, c) + u
    a = a.rotateLeft(30)

    c = c + d.rotateLeft(5) + bool3ary232(e, a, b) + v
    e = e.rotateLeft(30)

    b = b + c.rotateLeft(5) + bool3ary232(d, e, a) + w
    d = d.rotateLeft(30)

    return uintArrayOf(b, c, d, e)
}

private fun rounds4(h0: UIntArray, h1: UIntArray, wk: UIntArray, i: Int): UIntArray =
    sha1DigestRoundX4(h0, sha1FirstHalf(h1, wk), i)

private fun schedule(v0: UIntArray, v1: UIntArray, v2: UIntArray, v3: UIntArray): UIntArray =
    sha1msg2(xor(sha1msg1(v0, v1), v2), v3)

private fun sha1DigestBlockU32(state: UIntArray, block: UIntArray) {
    var w0 = uintArrayOf(block[0], block[1], block[2], block[3])
    var w1 = uintArrayOf(block[4], block[5], block[6], block[7])
    var w2 = uintArrayOf(block[8], block[9], block[10], block[11])
    var w3 = uintArrayOf(block[12], block[13], block[14], block[15])
    var w4 = UIntArray(4)

    var h0 = uintArrayOf(state[0], state[1], state[2], state[3])
    var h1 = sha1FirstAdd(state[4], w0)

    // Rounds 0..20
    h1 = sha1DigestRoundX4(h0, h1, 0)
    h0 = rounds4(h1, h0, w1, 0)
    h1 = rounds4(h0, h1, w2, 0)
    h0 = rounds4(h1, h0, w3, 0)
    w4 = schedule(w0, w1, w2, w3); h1 = rounds4(h0, h1, w4, 0)

    // Rounds 20..40
    w0 = schedule(w1, w2, w3, w4); h0 = rounds4(h1, h0, w0, 1)
    w1 = schedule(w2, w3, w4, w0); h1 = rounds4(h0, h1, w1, 1)
    w2 = schedule(w3, w4, w0, w1); h0 = rounds4(h1, h0, w2, 1)
    w3 = schedule(w4, w0, w1, w2); h1 = rounds4(h0, h1, w3, 1)
    w4 = schedule(w0, w1, w2, w3); h0 = rounds4(h1, h0, w4, 1)

    // Rounds 40..60
    w0 = schedule(w1, w2, w3, w4); h1 = rounds4(h0, h1, w0, 2)
    w1 = schedule(w2, w3, w4, w0); h0 = rounds4(h1, h0, w1, 2)
    w2 = schedule(w3, w4, w0, w1); h1 = rounds4(h0, h1, w2, 2)
    w3 = schedule(w4, w0, w1, w2); h0 = rounds4(h1, h0, w3, 2)
    w4 = schedule(w0, w1, w2, w3); h1 = rounds4(h0, h1, w4, 2)

    // Rounds 60..80
    w0 = schedule(w1, w2, w3, w4); h0 = rounds4(h1, h0, w0, 3)
    w1 = schedule(w2, w3, w4, w0); h1 = rounds4(h0, h1, w1, 3)
    w2 = schedule(w3, w4, w0, w1); h0 = rounds4(h1, h0, w2, 3)
    w3 = schedule(w4, w0, w1, w2); h1 = rounds4(h0, h1, w3, 3)
    w4 = schedule(w0, w1, w2, w3); h0 = rounds4(h1, h0, w4, 3)

    val e = h1[0].rotateLeft(30)
    val a = h0[0]
    val b = h0[1]
    val c = h0[2]
    val d = h0[3]

    state[0] = state[0] + a
    state[1] = state[1] + b
    state[2] = state[2] + c
    state[3] = state[3] + d
    state[4] = state[4] + e
}

internal fun compressSoft(state: UIntArray, blocks: Array<ByteArray>) {
    val blockU32 = UIntArray(BLOCK_SIZE / 4)
    // since LLVM can't properly use aliasing yet it will make
    // unnecessary state stores without this copy
    val stateCpy = state.copyOf()
    for (block in blocks) {
        var idx = 0
        var off = 0
        while (idx < blockU32.size) {
            blockU32[idx] =
                (block[off].toUByte().toUInt() shl 24) or
                    (block[off + 1].toUByte().toUInt() shl 16) or
                    (block[off + 2].toUByte().toUInt() shl 8) or
                    block[off + 3].toUByte().toUInt()
            idx++
            off += 4
        }
        sha1DigestBlockU32(stateCpy, blockU32)
    }
    for (i in state.indices) state[i] = stateCpy[i]
}
