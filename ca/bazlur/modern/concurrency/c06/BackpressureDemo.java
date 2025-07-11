package ca.bazlur.modern.concurrency.c06;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class BackpressureDemo {

    public static void main(String[] args) throws InterruptedException {
        // Simulate ultra-high-frequency price feed ①
        Flux<PriceData> extremeFeed = Flux.interval(Duration.ofNanos(100_000))
                .map(i -> new PriceData(
                        "HFT-Exchange",
                        "BTC/USD",
                        50000 + ThreadLocalRandom.current().nextDouble(-100, 100),
                        Instant.now()
                ))
                .share(); // Hot stream shared among subscribers

        // Strategy 1: Sampling - Take periodic snapshots ②
        System.out.println("SAMPLING Strategy:");
        extremeFeed
                .sample(Duration.ofMillis(100))
                .take(10)
                .subscribe(price -> System.out.printf(
                        "[SAMPLED] Price: $%.2f at %s%n",
                        price.price(), price.timestamp()));

        Thread.sleep(1500);

        // Strategy 2: Drop - Discard when overwhelmed ③
        System.out.println("\nDROP Strategy:");
        AtomicInteger dropped = new AtomicInteger(0);
        extremeFeed
                .onBackpressureDrop(price -> {
                    if (dropped.incrementAndGet() % 1000 == 0) {
                        System.out.printf("[DROPPED] %d updates dropped%n",
                                dropped.get());
                    }
                })
                .publishOn(Schedulers.boundedElastic())  // ④
                .take(Duration.ofSeconds(1))
                .subscribe(price -> {
                    simulateWork(10);  // Simulate slow processing
                    System.out.printf("[PROCESSED] Price: $%.2f%n", price.price());
                });

        Thread.sleep(1500);

        // Strategy 3: Latest - Keep only most recent value ⑤
        System.out.println("\nLATEST Strategy:");
        extremeFeed
                .onBackpressureLatest()
                .publishOn(Schedulers.boundedElastic())
                .subscribe(price -> {
                    simulateWork(10);  // Very slow processing
                    System.out.printf("[LATEST] Price: $%.2f%n", price.price());
                });

        Thread.sleep(2000);
    }

    private static void simulateWork(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public record PriceData(String exchange, String symbol, double price, Instant timestamp) {
    }
}
