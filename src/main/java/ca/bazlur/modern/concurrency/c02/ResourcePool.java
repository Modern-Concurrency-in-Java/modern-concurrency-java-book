package ca.bazlur.modern.concurrency.c02;

import java.util.Optional;
import java.util.concurrent.Semaphore;

public class ResourcePool {

    private final Semaphore semaphore; // ①

    public ResourcePool(int resourceCount) {
        this.semaphore = new Semaphore(resourceCount); // ②
    }

    public Optional<String> useResource(String query) {
        try {
            semaphore.acquire(); // ③
            try {
                // Simulate obtaining and using a database connection
                return queryDatabase(query); // ④
            } finally {
                semaphore.release(); // ⑤
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // ⑥
            return Optional.empty();
        }
    }

    private Optional<String> queryDatabase(String query) {
        // Simulate database query with some delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
        return Optional.of("Result for: " + query);
    }
}
