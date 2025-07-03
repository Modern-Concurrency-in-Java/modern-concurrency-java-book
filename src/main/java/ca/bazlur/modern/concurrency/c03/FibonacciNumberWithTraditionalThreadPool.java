package ca.bazlur.modern.concurrency.c03;

import java.util.Map;
import java.util.concurrent.*;

public class FibonacciNumberWithTraditionalThreadPool {

    private static final Map<Integer, Long> cache = new ConcurrentHashMap<>(
            Map.of(0, 0L, 1, 1L));

    private static long getFibonacci(int i, ExecutorService pool) {
        if (cache.containsKey(i)) {
            return cache.get(i);
        }

        Future<Long> future1 = pool.submit(() -> getFibonacci(i - 1, pool)); // ①
        Future<Long> future2 = pool.submit(() -> getFibonacci(i - 2, pool)); // ②

        try {
            long l1 = future1.get(); // ③
            long l2 = future2.get();
            long result = l1 + l2;
            cache.put(i, result);
            return result;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try (var pool = Executors.newFixedThreadPool(100)) { // ④
            Future<Long> future = pool.submit(() -> getFibonacci(20, pool));
            Long l = future.get();
            System.out.println("Fibonacci number is: " + l);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
