package io.burst.core;

/**
 * Immutable value object representing the outcome of a Burst benchmark.
 *
 * Each result captures the measured p95 latency, the allowed threshold
 * after scaling, the applied environment scale factor, and the pass/fail
 * decision. Results are serialized to JSON at JVM shutdown.
 *
 * @author Muhammed Hilal
 */

public record BurstResult(
        String testClass,
        String testMethod,
        double p95,
        double allowed,
        double scale,
        boolean passed
) {}