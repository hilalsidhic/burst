![Build](https://github.com/hilalsidhic/burst/actions/workflows/burst.yml/badge.svg)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.hilalsidhic/burst-junit.svg)](
https://search.maven.org/artifact/io.github.hilalsidhic/burst-junit
)

# Burst


**JUnit-native performance regression assertions for Java, powered by JMH.**

Burst allows developers to express **performance expectations directly inside JUnit tests** and fail the build when performance regresses — without writing JMH benchmarks or managing baselines.

---
## Why Burst?

Because performance regressions should fail tests,
not appear months later in production.

## What Burst Is

Burst is a **performance regression detector**, not a benchmark suite.

It answers one question:

> *“Did this code get slower than what I am willing to tolerate?”*

Burst integrates **JMH’s statistical rigor** with **JUnit’s developer experience**.

---

## What Burst Is NOT

- ❌ Not a load-testing tool
- ❌ Not a benchmarking framework
- ❌ Not a latency SLA enforcer
- ❌ Not a replacement for JMH

Burst intentionally focuses on **small, repeatable, CI-safe performance checks**.

---

## Installation

Burst is published to **Maven Central**.

**Maven Central**

### Maven

```xml

<dependencies>
  <dependency>
    <groupId>io.github.hilalsidhic</groupId>
    <artifactId>burst-junit</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>

```

### Gradle

```gradle
repositories {
    mavenCentral()
}

dependencies {
    // Burst
    testImplementation 'io.github.hilalsidhic:burst-junit:1.0.0'
}

test {
    useJUnitPlatform()
}

## Quick Start

1. Add the dependency
2. Annotate a JUnit test with `@BurstBenchmark`
3. Run tests normally

```bash
./gradlew test
```

Burst automatically:
- calibrates the environment
- runs the benchmark in a forked JVM
- fails the test on regression

---

## Basic Usage

```java
@Test
@BurstBenchmark(p95Millis = 50)
void createOrder() {
    service.createOrder();
}
```

That’s it.

If the measured **p95 latency** exceeds the allowed threshold (after environment scaling), the test fails.

---

## What `@BurstBenchmark` Means

```java
@BurstBenchmark(
    p95Millis = 50,
    warmup = 1,
    iterations = 2,
    maxScale = 1.5
)
```

### Semantics

- `p95Millis` is a **performance regression budget**
- It is **not** a promise of absolute speed
- The test fails only when:

```
measured p95 > p95Millis × environment scale
```

Burst detects **regressions**, not raw latency violations.

---

## How Burst Works (Execution Model)

```
JUnit Test
   │
   ▼
BurstExtension (JUnit InvocationInterceptor)
   │
   ├─ Environment calibration (JMH probe)
   │
   ├─ Forked JVM benchmark (JMH)
   │
   ▼
p95 assertion → PASS / FAIL
```

### Forking & Warmup Semantics

- Each `@BurstBenchmark` is executed in a **forked JVM**
- Warmup and measurement iterations are handled by **JMH**
- JVM state is not shared across benchmarks
- Calibration runs once per test JVM and is cached

---

## Environment Calibration

CI machines are slow and noisy.

Burst compensates by measuring **relative JVM performance degradation** using a lightweight probe and computing an **environment scale factor**.

Example:

```
baseline p95 = 2.0 ms
current  p95 = 6.0 ms
scale        = 3.0
```

The final allowed threshold becomes:

```
p95Millis × scale
```

Scaling is **bounded** using `maxScale` to prevent masking real regressions.

---

## JSON Results Output

After the test run, Burst writes a machine-readable report:

```json
[
  {
    "test": "io.example.OrderServiceTest#createOrder",
    "p95": 33.21,
    "allowed": 50.00,
    "scale": 1.00,
    "passed": true
  }
]
```

This enables:
- CI annotations
- Custom reporting
- Optional trend analysis

---

## Failure Example

```
Performance regression detected

Test:
  OrderServiceTest#createOrder

Measured:
  p95 = 34.1 ms

Allowed:
  25.0 ms (scale = 1.0)

Action:
  Investigate the regression or update @BurstBenchmark intentionally.
```

---

## CI Usage

Burst is CI-safe by default.

To disable Burst entirely:

```bash
./gradlew test -Dburst.enabled=false
```

All tests will pass without running JMH.

---

## Constraints

Because Burst executes benchmarks in a **forked JVM via JMH**, the following constraints apply:

- Test classes must be `public`
- Test classes must have a no-argument constructor
- Benchmark methods must take no parameters
- No shared mutable state between benchmark methods
- No asynchronous work inside benchmark methods

These constraints ensure **correctness, isolation, and reproducibility**.

---

## When NOT to Use Burst

Burst is not suitable when:
- You need high-concurrency load testing
- You need historical trend analysis across versions
- You require system-wide SLA enforcement

Use dedicated load-testing or benchmarking tools in those cases.

---

## Design Principles

- **JUnit-first** developer experience
- **JMH-level measurement correctness**
- **No historical baselines**
- **Deterministic CI behavior**
- **Opinionated, minimal API**

Burst chooses **correctness over convenience**.

---

## Compatibility

- Java: 21+
- JUnit: 5.x
- Build tools: Gradle, Maven
- CI: GitHub Actions, GitLab CI, Jenkins

---

## Status

**Burst v1.0**

- Core engine complete
- Environment calibration implemented
- CI-safe execution
- JSON reporting
- Public API frozen

---

## License

Apache License 2.0
