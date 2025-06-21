package ca.bazlur.modern.concurrency.c02;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class LittleLawExample {
    public static void main(String[] args) {
        int numTasks = 10000; // ①
        int avgResponseTimeMillis = 500; // Average task response time // ②

        // Simulate adjustable I/O-bound work
        Runnable ioBoundTask = () -> {
            try {
                Thread.sleep(Duration.ofMillis(avgResponseTimeMillis)); // ③
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        System.out.println("=== Little's Law Throughput Comparison ===");
        System.out.println("Testing " + numTasks + " tasks with " + avgResponseTimeMillis + "ms latency each\n");

        benchmark("Virtual Threads",
                Executors.newVirtualThreadPerTaskExecutor(), ioBoundTask, numTasks);
        benchmark("Fixed ThreadPool (100)",
                Executors.newFixedThreadPool(100), ioBoundTask, numTasks);
        benchmark("Fixed ThreadPool (500)",
                Executors.newFixedThreadPool(500), ioBoundTask, numTasks);
        benchmark("Fixed ThreadPool (1000)",
                Executors.newFixedThreadPool(1000), ioBoundTask, numTasks);
    }

    static void benchmark(String type, ExecutorService executor, Runnable task, int numTasks) {
        Instant start = Instant.now(); // ④
        AtomicLong completedTasks = new AtomicLong();

        try (executor) { // ⑤
            IntStream.range(0, numTasks)
                    .forEach(_ -> executor.submit(() -> {
                        task.run();
                        completedTasks.incrementAndGet(); // ⑥
                    }));
        } // ⑦

        Instant end = Instant.now();
        long duration = Duration.between(start, end).toMillis();
        double throughput = (double) completedTasks.get() / duration * 1000; // Tasks per second // ⑧

        System.out.printf("%-25s - Time: %5dms, Throughput: %8.2f tasks/s%n",
                type, duration, throughput);
    }
}
