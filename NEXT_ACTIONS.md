# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 6/6 (100.0%)
- **Function parity:** 23/23 matched (target 38) — 100.0%
- **Class/type parity:** 2/5 matched (target 2) — 40.0%
- **Combined symbol parity:** 25/28 matched (target 40) — 89.3%
- **Average inline-code cosine:** 0.45 (function body across 6 matched files)
- **Average documentation cosine:** 0.30 (doc text across 6 matched files)
- **Cheat-zeroed Files:** 1
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

- **Target:** `sha1.Lib [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 31110.0
- **Functions:** 6/6 matched (target 15)
- **Missing functions:** _none_
- **Types:** 2/5 matched (target 2)
- **Missing types:** `BlockSize`, `BufferKind`, `OutputSize`

### 3. compress.soft

- **Target:** `compress.Soft`
- **Similarity:** 0.65
- **Dependents:** 0
- **Priority Score:** 1203.5
- **Functions:** 12/12 matched (target 18)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 4. compress.x86

- **Target:** `compress.X86`
- **Similarity:** 0.54
- **Dependents:** 0
- **Priority Score:** 204.6
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 5. compress.loongarch64_asm

- **Target:** `compress.Loongarch64Asm`
- **Similarity:** 0.10
- **Dependents:** 0
- **Priority Score:** 109.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 6. compress.aarch64

- **Target:** `compress.Aarch64`
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

