package ca.bazlur.modern.concurrency.c03;

import java.util.Map;
import java.util.concurrent.*;

public class CallableExample {

    static final Map<Integer, Long> cache = new ConcurrentHashMap<>(
            Map.of(0, 0L, 1, 1L) // ①
    );

    public static void main(String[] args) throws Exception {
        try (ExecutorService threadPool = Executors.newCachedThreadPool()) {
            Future<Long> fibonacciNumber = threadPool.submit(new Callable<Long>() { // ②
                @Override
                public Long call() throws Exception {
                    return fibonacci(50); // ③
                }
            });
        }
    }

    private static Long fibonacci(int n) {
        if (cache.containsKey(n)) {
            return cache.get(n);
        } else {
            long result = fibonacci(n - 1) + fibonacci(n - 2);
            cache.put(n, result); // ④
            return result;
        }
    }
}
