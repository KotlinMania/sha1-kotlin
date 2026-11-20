# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 5/6 (83.3%)
- **Function parity:** 17/17 matched (target 25) — 100.0%
- **Class/type parity:** 0/0 matched (target 1) — N/A
- **Combined symbol parity:** 17/17 matched (target 26) — 100.0%
- **Average inline-code cosine:** 0.19 (function body across 5 matched files)
- **Average documentation cosine:** 0.30 (doc text across 5 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 5 files with <0.60 function similarity
- **Needs Review:** 0 files with 0.60-0.84 function similarity
- **Excellent:** 0 files with >=0.85 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. compress

- **Target:** `compress.Compress`
- **Similarity:** 0.16
- **Dependents:** 1
- **Priority Score:** 1000108.4
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 2. compress.soft

- **Target:** `soft.Soft`
- **Similarity:** 0.38
- **Dependents:** 0
- **Priority Score:** 1206.2
- **Functions:** 12/12 matched (target 19)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 3. compress.x86

- **Target:** `x86.X86`
- **Similarity:** 0.21
- **Dependents:** 0
- **Priority Score:** 207.9
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 4. compress.loongarch64_asm

- **Target:** `loongarch64asm.Loongarch64Asm`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 110.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 5. compress.aarch64

- **Target:** `aarch64.Aarch64`
- **Similarity:** 0.18
- **Dependents:** 0
- **Priority Score:** 108.2
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

## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `lib` | `Lib` | 0 | `lib.rs` | `Lib.kt` |

