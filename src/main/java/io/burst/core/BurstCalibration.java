package io.burst.core;

import io.burst.jmh.EnvironmentProbe;
import io.burst.jmh.JmhRunner;

/**
 * Computes and caches the environment performance scale factor.
 *
 * Burst calibration compares a baseline probe execution against the
 * current execution environment to estimate relative performance
 * degradation. The resulting scale factor is applied to all regression
 * thresholds during a test run.
 *
 * Calibration is performed once per JVM and cached for subsequent tests.
 *
 * @author Muhammed Hilal
 */

public final class BurstCalibration {

    private static volatile Double cachedScale;

    private BurstCalibration() {}

    public static double environmentScale() {
        if (cachedScale != null) return cachedScale;

        synchronized (BurstCalibration.class) {
            if (cachedScale != null) return cachedScale;

            try {
                // Baseline: fast, minimal interference
                double baseline = JmhRunner.runProbe(
                        EnvironmentProbe.class,
                        1, 1
                );

                // Current: realistic environment
                double current = JmhRunner.runProbe(
                        EnvironmentProbe.class,
                        1, 2
                );

                if (baseline <= 0) {
                    cachedScale = 1.0;
                    return cachedScale;
                }

                double rawScale = current / baseline;
                double boundedScale = Math.max(1.0, rawScale);

                System.out.printf("""
                [Burst] Calibration:
                 baseline p95 = %.2f ms
                 current  p95 = %.2f ms
                 raw environment scale = %.2f
                %n""", baseline, current, rawScale);

                cachedScale = boundedScale;
                return cachedScale;

            } catch (Throwable t) {
                cachedScale = 1.0;
                return cachedScale;
            }
        }
    }
}
