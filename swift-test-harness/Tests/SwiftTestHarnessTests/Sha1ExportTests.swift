import Testing
import Sha1

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
}
