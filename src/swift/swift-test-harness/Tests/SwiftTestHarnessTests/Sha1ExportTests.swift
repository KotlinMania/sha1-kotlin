import Testing
import KotlinStdlib
import Sha1

private func makeKotlinByteArray(_ bytes: [UInt8]) -> ExportedKotlinPackages.kotlin.ByteArray {
    ExportedKotlinPackages.kotlin.ByteArray(size: Int32(bytes.count)) { idx in
        Int8(bitPattern: bytes[Int(idx)])
    }
}

private func toSwiftBytes(_ array: ExportedKotlinPackages.kotlin.ByteArray) -> [UInt8] {
    (0 ..< Int(array.size)).map { idx in
        UInt8(bitPattern: array._get(index: Int32(idx)))
    }
}

private func hexBytes(_ hex: String) -> [UInt8] {
    precondition(hex.count % 2 == 0)
    var result: [UInt8] = []
    result.reserveCapacity(hex.count / 2)
    var index = hex.startIndex
    while index < hex.endIndex {
        let next = hex.index(index, offsetBy: 2)
        result.append(UInt8(hex[index ..< next], radix: 16)!)
        index = next
    }
    return result
}

@Suite("Sha1 Swift Export Tests")
struct Sha1ExportTests {
    @Test("Swift module imports and executes SHA-1 compression via native cinterop")
    func swiftModuleExecutesSha1() throws {
        #expect(Sha1Core.Companion.shared.OID == "1.3.14.3.2.26")
        let hasher = Sha1()
        #expect(hasher.outputSize == 20)
        #expect(hasher.blockSize == 64)
        let digest = hasher.finalizeReset()
        #expect(digest.size == 20)
        #expect(UInt8(bitPattern: digest._get(index: 0)) == 0xda)
        #expect(UInt8(bitPattern: digest._get(index: 1)) == 0x39)
        #expect(UInt8(bitPattern: digest._get(index: 19)) == 0x09)
    }

    @Test("One-shot digest computes known SHA-1 vectors")
    func oneShotDigestVectors() {
        let abc = Sha1.Companion.shared.digest(data: makeKotlinByteArray(Array("abc".utf8)))
        #expect(toSwiftBytes(abc) == hexBytes("a9993e364706816aba3e25717850c26c9cd0d89d"))

        let hello = Sha1.Companion.shared.digest(data: makeKotlinByteArray(Array("hello world".utf8)))
        #expect(toSwiftBytes(hello) == hexBytes("2aae6c35c94fcfb415dbe95f408b9ce91ee846ed"))
    }

    @Test("Streaming update, finalizeReset, finalizeInto, reset, zeroize, and newWithPrefix match vectors")
    func streamingResetZeroizeAndPrefix() {
        let hasher = Sha1.Companion.shared.new()
        hasher.update(data: makeKotlinByteArray(Array("a".utf8)))
        hasher.update(data: makeKotlinByteArray(Array("bc".utf8)))
        #expect(toSwiftBytes(hasher.finalizeReset()) == hexBytes("a9993e364706816aba3e25717850c26c9cd0d89d"))

        hasher.update(data: makeKotlinByteArray(Array("ignored".utf8)))
        hasher.reset()
        #expect(toSwiftBytes(hasher.finalizeReset()) == hexBytes("da39a3ee5e6b4b0d3255bfef95601890afd80709"))

        hasher.update(data: makeKotlinByteArray(Array("hello world".utf8)))
        hasher.zeroize()
        let out = makeKotlinByteArray([UInt8](repeating: 0, count: 20))
        hasher.finalizeInto(out: out)
        #expect(toSwiftBytes(out) == hexBytes("da39a3ee5e6b4b0d3255bfef95601890afd80709"))

        let prefixed = Sha1.Companion.shared.newWithPrefix(data: makeKotlinByteArray(Array("The quick brown fox ".utf8)))
        prefixed.update(data: makeKotlinByteArray(Array("jumps over the lazy dog".utf8)))
        #expect(toSwiftBytes(prefixed.finalizeReset()) == hexBytes("2fd4e1c67a2d28fced849ee1bb76e7391b93eb12"))
    }

    @Test("Sha1Core default, writeAlgName, and fmt match expected metadata")
    func sha1CoreMetadata() {
        #expect(Sha1.Companion.shared.OID == "1.3.14.3.2.26")
        let core = Sha1Core.Companion.shared.`default`()
        #expect(core.blockSize == 64)
        #expect(core.outputSize == 20)
        #expect(core.writeAlgName() == "Sha1")
        #expect(core.fmt() == "Sha1Core { ... }")
        #expect(core.toString() == "Sha1Core { ... }")
    }
}
