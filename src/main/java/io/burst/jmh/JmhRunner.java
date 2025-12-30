package io.burst.jmh;

import io.burst.annotation.BurstBenchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Utility class responsible for configuring and executing JMH benchmarks
 * on behalf of Burst.
 *
 * This class encapsulates all JMH configuration, including fork isolation,
 * warmup, measurement iterations, and percentile extraction. It provides
 * a stable interface between Burst and the JMH runtime.
 *
 * @author Muhammed Hilal
 */

public final class JmhRunner {

    private JmhRunner() {}

    public static double runProbe(
            Class<?> probeClass,
            int warmup,
            int iterations
    ) throws Exception {

        Options options = new OptionsBuilder()
                .include(probeClass.getName())
                .mode(Mode.SampleTime)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .warmupIterations(warmup)
                .measurementIterations(iterations)
                .threads(1)
                .shouldFailOnError(true)
                .build();

        return extractP95(new Runner(options).run());
    }

    public static double runBenchmark(
            Class<?> bridge,
            BurstBenchmark cfg,
            Class<?> testClass,
            String methodName
    ) throws Exception {

        Options options = new OptionsBuilder()
                .include(bridge.getName())
                .mode(Mode.SampleTime)                 // ✅ REQUIRED
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .warmupIterations(cfg.warmup())
                .measurementIterations(cfg.iterations())
                .jvmArgs(
                        "-Dburst.class=" + testClass.getName(),
                        "-Dburst.method=" + methodName
                )
                .threads(1)
                .shouldFailOnError(true)
                .build();

        return extractP95(new Runner(options).run());
    }

    private static double extractP95(Collection<RunResult> results) {
        return results.iterator()
                .next()
                .getPrimaryResult()
                .getStatistics()
                .getPercentile(95);
    }
}
