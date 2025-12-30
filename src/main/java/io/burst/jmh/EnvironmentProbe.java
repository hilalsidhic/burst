package io.burst.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Lightweight JMH benchmark used to measure relative JVM and system
 * performance characteristics.
 *
 * The probe exercises CPU, memory allocation, and scheduler behavior
 * to estimate environment-induced performance degradation, particularly
 * in CI environments.
 *
 * The measured p95 latency is used to compute an environment scale factor
 * that adjusts regression thresholds.
 *
 * @author Muhammed Hilal
 */

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class EnvironmentProbe {

    private byte[] buffer;

    @Setup(Level.Trial)
    public void setup() {
        buffer = new byte[1024];
    }

    @Benchmark
    public void probe(Blackhole bh) {
        // CPU
        Blackhole.consumeCPU(10_000);

        // Memory (allocation-free)
        bh.consume(buffer);

        // Scheduling jitter
        LockSupport.parkNanos(1_000_000); // ~1ms
    }
}
