package ca.bazlur.modern.concurrency.c03;

import java.util.Map;
import java.util.concurrent.*;

public class FibonacciNumberWithForkJoinPool {

    private static final Map<Integer, Long> cache = new ConcurrentHashMap<>(
            Map.of(0, 0L, 1, 1L));

    static class FibonacciTask extends RecursiveTask<Long> { // ①
        private final int n;

        public FibonacciTask(int n) {
            this.n = n;
        }

        @Override
        protected Long compute() {
            if (cache.containsKey(n)) {
                return cache.get(n);
            }

            FibonacciTask f1 = new FibonacciTask(n - 1);
            f1.fork(); // ②
            FibonacciTask f2 = new FibonacciTask(n - 2);
            long result = f2.compute() + f1.join(); // ③
            cache.put(n, result);
            return result;
        }
    }

    public static void main(String[] args) {
        try (var pool = new ForkJoinPool()) { // ④
            Long result = pool.invoke(new FibonacciTask(20));
            System.out.println("Fibonacci number is: " + result);
        }
    }
}
