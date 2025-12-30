package io.burst.annotation;

import io.burst.core.BurstExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a performance regression assertion for a JUnit test method.
 *
 * Methods annotated with {@code @BurstBenchmark} are intercepted by Burst
 * and executed as isolated JMH benchmarks in a forked JVM. The test fails
 * if the measured p95 latency exceeds the allowed threshold after applying
 * environment scaling.
 *
 * <p>{@code p95Millis} represents a performance regression budget, not an
 * absolute performance guarantee.</p>
 *
 * <p><b>Constraints:</b></p>
 * <ul>
 *   <li>Test method must have no parameters</li>
 *   <li>No overloaded benchmark methods</li>
 *   <li>No shared mutable state</li>
 *   <li>No asynchronous or background work</li>
 * </ul>
 *
 * <p>These constraints are intentional and required for correctness,
 * isolation, and reproducibility.</p>
 *
 * @author Muhammed Hilal
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@ExtendWith(BurstExtension.class)
public @interface BurstBenchmark {

    /**
     * Maximum allowed p95 latency (in milliseconds) before the test is
     * considered a performance regression.
     */
    long p95Millis();

    /**
     * Number of warmup iterations executed by JMH before measurement.
     */
    int warmup() default 1;

    /**
     * Number of measurement iterations executed by JMH.
     */
    int iterations() default 2;

    /**
     * Upper bound on the environment scale factor applied during CI
     * calibration.
     */
    double maxScale() default 1.5;
}
