# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 6/6 (100.0%)
- **Function parity:** 22/23 matched (target 49) — 95.7%
- **Class/type parity:** 2/5 matched (target 3) — 40.0%
- **Combined symbol parity:** 24/28 matched (target 52) — 85.7%
- **Average inline-code cosine:** 0.50 (function body across 6 matched files)
- **Average documentation cosine:** 0.30 (doc text across 6 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 3 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. compress

- **Target:** `compress.Compress`
- **Similarity:** 0.73
- **Dependents:** 1
- **Priority Score:** 1000102.8
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 2. lib

- **Target:** `sha1.Lib`
- **Similarity:** 0.30
- **Dependents:** 0
- **Priority Score:** 41107.0
- **Functions:** 5/6 matched (target 22)
- **Missing functions:** `finalize_fixed_core`
- **Types:** 2/5 matched (target 2)
- **Missing types:** `BlockSize`, `BufferKind`, `OutputSize`

### 3. compress.soft

- **Target:** `soft.Soft [PROVENANCE-FALLBACK]`
- **Similarity:** 0.65
- **Dependents:** 0
- **Priority Score:** 1203.5
- **Functions:** 12/12 matched (target 22)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/compress/soft.rs` vs expected `compress/soft.rs`
- **Proposed provenance header:** `// port-lint: tests compress/soft.rs` (current: `// port-lint: tests src/compress/soft.rs`)
- **Lint issues:** 1

### 4. compress.x86

- **Target:** `x86.X86`
- **Similarity:** 0.54
- **Dependents:** 0
- **Priority Score:** 204.6
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 5. compress.loongarch64_asm

- **Target:** `loongarch64asm.Loongarch64Asm`
- **Similarity:** 0.10
- **Dependents:** 0
- **Priority Score:** 109.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 6. compress.aarch64

- **Target:** `aarch64.Aarch64`
- **Similarity:** 0.71
- **Dependents:** 0
- **Priority Score:** 102.9
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

