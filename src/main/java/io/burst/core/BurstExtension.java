package io.burst.core;

import io.burst.annotation.BurstBenchmark;
import io.burst.jmh.BurstJmhBridge;
import io.burst.jmh.JmhRunner;
import org.junit.jupiter.api.extension.*;

import java.io.BufferedWriter;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JUnit 5 extension that intercepts {@code @BurstBenchmark} test methods
 * and delegates their execution to JMH.
 *
 * This extension prevents direct execution of the test body, performs
 * environment calibration, launches a forked JMH benchmark, evaluates
 * the regression threshold, and records the result.
 *
 * Each benchmark is executed in an isolated JVM to ensure statistical
 * correctness and reproducibility.
 *
 * @author Muhammed Hilal
 */

public final class BurstExtension implements InvocationInterceptor {

    private static final List<BurstResult> RESULTS =
            Collections.synchronizedList(new ArrayList<>());

    @Override
    public void interceptTestMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> ctx,
            ExtensionContext ec
    ) throws Throwable {

        boolean enabled = Boolean.parseBoolean(
                System.getProperty("burst.enabled", "true")
        );
        if (!enabled) {
            invocation.skip();
            return;
        }


        Method method = ctx.getExecutable();
        BurstBenchmark cfg = method.getAnnotation(BurstBenchmark.class);

        double scale = BurstCalibration.environmentScale();
        double boundedScale = Math.min(scale, cfg.maxScale());

        System.out.printf(
                "[Burst] Effective scale for %s#%s = %.2f (maxScale = %.2f)%n",
                method.getDeclaringClass().getSimpleName(),
                method.getName(),
                boundedScale,
                cfg.maxScale()
        );

        double p95 = JmhRunner.runBenchmark(
                BurstJmhBridge.class,
                cfg,
                method.getDeclaringClass(),
                method.getName()
        );

        double allowed = cfg.p95Millis() * boundedScale;

        RESULTS.add(new BurstResult(
                method.getDeclaringClass().getName(),
                method.getName(),
                p95,
                allowed,
                boundedScale,
                p95 <= allowed
        ));

        if (p95 > allowed) {
            throw new AssertionError(String.format("""
                [Burst] Performance regression detected
                
                Test:
                  %s#%s
                
                Measured:
                  p95 = %.2f ms
                
                Allowed:
                  %.2f ms (effective scale = %.2f)
                
                Action:
                  Investigate the regression or update @BurstBenchmark intentionally.
                """,
                                    method.getDeclaringClass().getName(),
                                    method.getName(),
                                    p95,
                                    allowed,
                                    boundedScale
            ));
        }

        invocation.skip();
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Path out = Paths.get("target", "burst-results.json");
            try {
                Files.createDirectories(out.getParent());
            } catch (Exception ignored) {}
            try (BufferedWriter w = Files.newBufferedWriter(out)) {
                w.write("[\n");
                for (int i = 0; i < RESULTS.size(); i++) {
                    BurstResult r = RESULTS.get(i);
                    w.write(String.format(
                            """
                            {
                              "test": "%s#%s",
                              "p95": %.2f,
                              "allowed": %.2f,
                              "scale": %.2f,
                              "passed": %s
                            }%s
                            """,
                            r.testClass(),
                            r.testMethod(),
                            r.p95(),
                            r.allowed(),
                            r.scale(),
                            r.passed(),
                            (i < RESULTS.size() - 1) ? "," : ""
                    ));
                }
                w.write("\n]");
            } catch (Exception e) {

            }
        }));
    }
}