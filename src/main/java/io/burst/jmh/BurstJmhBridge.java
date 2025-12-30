package io.burst.jmh;

import org.openjdk.jmh.annotations.*;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Adapter that bridges JUnit test methods to JMH benchmarks.
 *
 * JMH cannot benchmark arbitrary methods directly. This class resolves
 * the target test method via reflection and invokes it from within a
 * JMH {@code @Benchmark} method.
 *
 * The bridge runs inside a forked JVM and recreates the test instance
 * using a no-argument constructor. Test classes must therefore be public
 * and instantiable.
 *
 * @author Muhammed Hilal
 */

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BurstJmhBridge {

    private Method target;
    private Object instance;

    @Setup(Level.Trial)
    public void setup() throws Exception {

        String className = System.getProperty("burst.class");
        String methodName = System.getProperty("burst.method");

        if (className == null || methodName == null) {
            throw new IllegalStateException(
                    "BurstJmhBridge requires -Dburst.class and -Dburst.method"
            );
        }

        Class<?> clazz = Class.forName(className);

        // Find the exact method (no args only – enforced by Burst)
        target = clazz.getDeclaredMethod(methodName);
        target.setAccessible(true);

        /*
         * IMPORTANT:
         * JUnit already creates the test instance.
         * We recreate it here ONLY because this runs in a forked JVM.
         *
         * Requirement: test class must have a no-arg constructor.
         * This is a conscious, documented constraint.
         */
        instance = clazz.getDeclaredConstructor().newInstance();
    }

    @Benchmark
    public void invoke() throws Throwable {
        target.invoke(instance);
    }
}
