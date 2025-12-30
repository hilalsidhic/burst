# Burst

**JUnit-native performance regression assertions for Java, powered by JMH.**

Burst allows developers to express **performance expectations directly inside JUnit tests** and fail the build when performance regresses — without writing JMH benchmarks or managing baselines.

---

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

### Key Properties

- Each Burst test runs in a **fresh JVM fork**
- Warmup and measurement are handled by **JMH**
- Results are statistically meaningful (sampling + percentiles)
- No test body is executed directly by JUnit

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
- Trend analysis (optional)

---

## CI Usage

Burst is CI-safe by default.

To disable Burst entirely (escape hatch):

```bash
./gradlew test -Dburst.enabled=false
```

All tests will pass without running JMH.

---

## Constraints (Important)

Because Burst executes benchmarks in a **forked JVM via JMH**, the following constraints apply:

- Test classes must be `public`
- Test classes must have a no-argument constructor
- Benchmark methods must take no parameters
- No shared mutable state between benchmark methods
- No asynchronous work inside benchmark methods

These constraints ensure **correctness, isolation, and reproducibility**.

---

## Design Principles

- **JUnit-first** developer experience
- **JMH-level measurement correctness**
- **No historical baselines**
- **Deterministic CI behavior**
- **Opinionated, minimal API**

Burst chooses **correctness over convenience**.

---

## Status

**Burst v1.0**

- Core engine complete
- Calibration implemented
- CI-safe execution
- JSON reporting
- Public API frozen

---

## License

MIT
