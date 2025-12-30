package io.burst.examples;

public class OrderService {

    public void createOrder() {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
