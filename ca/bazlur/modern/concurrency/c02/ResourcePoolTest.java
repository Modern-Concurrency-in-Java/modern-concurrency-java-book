package ca.bazlur.modern.concurrency.c02;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class ResourcePoolTest {
    public static void main(String[] args) throws Exception {
        int maxConcurrentThreads = 5;
        int totalRequests = 50;
        MonitoredResourcePool pool = new MonitoredResourcePool(maxConcurrentThreads);

        List<Future<Optional<String>>> futures = new ArrayList<>();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < totalRequests; i++) {
                final int taskId = i;
                futures.add(executor.submit(() -> pool.useResource("Query " + taskId)));
            }

            int successCount = 0;
            int timeoutCount = 0;

            for (Future<Optional<String>> future : futures) {
                Optional<String> result = future.get();
                if (result.isPresent()) {
                    successCount++;
                } else {
                    timeoutCount++; // ①
                }
            }

            System.out.printf("Total requests: %d%n", totalRequests);
            System.out.printf("Successful: %d%n", successCount);
            System.out.printf("Timed out: %d%n", timeoutCount);
            System.out.printf("Peak concurrent connections: %d%n",
                    pool.getPeakConnections()); // ②

            assert pool.getPeakConnections() <= maxConcurrentThreads : "Peak connections exceeded limit!";
        }
    }
}
