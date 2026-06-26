#include "sha1_asm_wrapper.h"

#if defined(SHA1_KOTLIN_HAS_UPSTREAM_ASM)
extern "C" void sha1_compress(uint32_t state[5], const uint8_t block[64]);
#else
static uint32_t sha1_rotl(uint32_t value, uint32_t bits) {
    return (value << bits) | (value >> (32 - bits));
}

extern "C" void sha1_compress(uint32_t state[5], const uint8_t block[64]) {
    uint32_t w[80];
    for (uint32_t i = 0; i < 16; ++i) {
        const uint32_t offset = i * 4;
        w[i] =
            (static_cast<uint32_t>(block[offset]) << 24) |
            (static_cast<uint32_t>(block[offset + 1]) << 16) |
            (static_cast<uint32_t>(block[offset + 2]) << 8) |
            static_cast<uint32_t>(block[offset + 3]);
    }
    for (uint32_t i = 16; i < 80; ++i) {
        w[i] = sha1_rotl(w[i - 3] ^ w[i - 8] ^ w[i - 14] ^ w[i - 16], 1);
    }

    uint32_t a = state[0];
    uint32_t b = state[1];
    uint32_t c = state[2];
    uint32_t d = state[3];
    uint32_t e = state[4];

    for (uint32_t i = 0; i < 80; ++i) {
        uint32_t f;
        uint32_t k;
        if (i < 20) {
            f = (b & c) | ((~b) & d);
            k = 0x5A827999U;
        } else if (i < 40) {
            f = b ^ c ^ d;
            k = 0x6ED9EBA1U;
        } else if (i < 60) {
            f = (b & c) | (b & d) | (c & d);
            k = 0x8F1BBCDCU;
        } else {
            f = b ^ c ^ d;
            k = 0xCA62C1D6U;
        }
        const uint32_t temp = sha1_rotl(a, 5) + f + e + k + w[i];
        e = d;
        d = c;
        c = sha1_rotl(b, 30);
        b = a;
        a = temp;
    }

    state[0] += a;
    state[1] += b;
    state[2] += c;
    state[3] += d;
    state[4] += e;
}
#endif

extern "C" void sha1_asm_compress_blocks(
    uint32_t state[5],
    const uint8_t *blocks,
    uint32_t block_count
) {
    for (uint32_t i = 0; i < block_count; ++i) {
        sha1_compress(state, blocks + static_cast<uint64_t>(i) * 64U);
    }
}
