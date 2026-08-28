# port-lint Proposed Changes

**Generated:** 2026-08-28
**Source:** tmp/sha1/src
**Target:** src/commonMain/kotlin/io/github/kotlinmania/sha1

These are review proposals only. They are emitted when a Rust -> Kotlin pair matches only after fallback normalization, so the existing `port-lint` header is not an exact provenance match.

| Target file | Current header | Proposed header | Source path | Reason |
|-------------|----------------|-----------------|-------------|--------|
| `src/commonTest/kotlin/io/github/kotlinmania/sha1/compress/CompressTest.kt` | `// port-lint: tests src/compress/soft.rs` | `// port-lint: tests compress/soft.rs` | `compress/soft.rs` | `port-lint provenance header matched only after fallback normalization: 'tests:src/compress/soft.rs' vs expected 'compress/soft.rs'` |
