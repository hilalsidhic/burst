package io.burst.examples;

import io.burst.annotation.BurstBenchmark;
import org.junit.jupiter.api.Test;

public class OrderServicePerformanceTest {

    private final io.burst.examples.OrderService service = new io.burst.examples.OrderService();

    @Test
    @BurstBenchmark(
            p95Millis = 50,
            warmup = 1,
            iterations = 2
    )
    void createOrder_pass() {
        service.createOrder();
    }

//    @Test
//    @BurstBenchmark(p95Millis = 5)
//    void createOrder_fail() {
//        service.createOrder();
//    }

    @Test
    @BurstBenchmark(
            p95Millis = 50,
            maxScale = 1.5
    )
    void createOrder_withNoise() {
        for (int i = 0; i < 20_000; i++) {
            Math.sqrt(i);
        }
        service.createOrder();
    }

//    @Test
//    @BurstBenchmark(p95Millis = 25)
//    void createOrder_tightBudget() {
//        service.createOrder();
//    }

    @Test
    @BurstBenchmark(p95Millis = 60)
    void createOrder_relaxedBudget() {
        service.createOrder();
    }
}
