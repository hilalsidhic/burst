# Burst v1.0.0 — Initial Public Release

Burst introduces a JUnit-native way to detect performance regressions in Java using JMH, designed specifically for CI environments.

This release focuses on correctness, determinism, and developer ergonomics.

---

## 🚀 Features

- **@BurstBenchmark annotation**
  - Declare p95 latency regression budgets directly in JUnit tests
- **JMH-backed execution**
  - Benchmarks run in a forked JVM with proper warmup and sampling
- **Environment calibration**
  - Automatically adjusts thresholds based on CI machine performance
  - Scaling is bounded to avoid masking real regressions
- **CI-safe by design**
  - Deterministic execution
  - Escape hatch via `-Dburst.enabled=false`
- **Machine-readable results**
  - JSON report generated after test execution

---

## 🧠 Design Philosophy

- Detect regressions, not absolute performance
- Avoid historical baselines
- Prefer correctness over convenience
- Keep the public API minimal and opinionated

---

## ⚠️ Constraints

Due to forked JVM execution via JMH:

- Test classes must be public
- No-argument constructor required
- No parameters in benchmark methods
- No shared mutable state
- No asynchronous or background work

These constraints are intentional to ensure isolation and reproducibility.

---

## 📦 Installation

```xml
<dependency>
  <groupId>io.burst</groupId>
  <artifactId>burst-junit</artifactId>
  <version>1.0.0</version>
</dependency>
```

(See README for GitHub Packages authentication details.)

---

## 🏷 Versioning

- Version: **1.0.0**
- Public API is considered stable
- Future releases will aim to remain backward-compatible

---

## 📄 License

Apache License 2.0
