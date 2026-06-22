# Porting Notes

## `proc-macro-hack` under `tmp/rustcrypto/`

The `tmp/rustcrypto/proc-macro-hack/` subtree is a vendored workspace sibling
of the RustCrypto organization, not a dependency of the `sha1` crate. The
`sha1` crate's `Cargo.toml` (`tmp/sha1/Cargo.toml`) lists only `cfg-if`,
`digest`, `cpufeatures`, and `sha1-asm` as dependencies — `syn` is not among
them.

`proc-macro-hack` is a deprecated Rust 1.31–1.45 compiler-plugin helper
(`[lib] proc-macro = true`) that lets function-like procedural macros be
invoked in expression position on older Rust compilers. It is superseded as
of Rust 1.45 by native `#[proc_macro]` in expression position.

`syn` appears only as a `[dev-dependencies]` entry (version `1.0.5`, syn 1.x)
in `proc-macro-hack/Cargo.toml`, used exclusively in a doc-comment example.
No actual source file in `proc-macro-hack` imports `syn`.

This crate is a Rust compile-time token-stream manipulator with no Kotlin
counterpart. Kotlin has no procedural-macro system of this kind; KSP and
compiler plugins are a different model entirely. Per AGENTS.md §3 "Common
sense applies," this is unportable and is not ported.

## Conclusion

No `syn-kotlin`, `proc-macro2-kotlin`, or `quote-kotlin` Maven dependency is
required for `sha1-kotlin`. The core sha1 hash implementation has no
relationship to `syn`.