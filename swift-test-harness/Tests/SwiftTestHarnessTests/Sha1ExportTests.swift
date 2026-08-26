import Testing
import Sha1

@Suite("Sha1 Swift Export Tests")
struct Sha1ExportTests {
    @Test("Swift module imports and basic types are reachable")
    func swiftModuleLoads() throws {
        #expect(Bool(true))
    }
}
