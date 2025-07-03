package ca.bazlur.modern.concurrency.c02;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MonitoredResourcePool {

    private final Semaphore semaphore;
    private final AtomicInteger activeConnections; // ①
    private final AtomicInteger peakConnections; // ②

    public MonitoredResourcePool(int resourceCount) {
        this.semaphore = new Semaphore(resourceCount, true); // ③
        this.activeConnections = new AtomicInteger(0);
        this.peakConnections = new AtomicInteger(0);
    }

    public Optional<String> useResource(String query) {
        boolean acquired = false;
        try {
            acquired = semaphore.tryAcquire(5, TimeUnit.SECONDS); // ④
            if (!acquired) {
                return Optional.empty(); // ⑤
            }

            int current = activeConnections.incrementAndGet();
            peakConnections.updateAndGet(peak -> Math.max(peak, current)); // ⑥

            return queryDatabase(query);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } finally {
            if (acquired) {
                activeConnections.decrementAndGet();
                semaphore.release();
            }
        }
    }

    public int getCurrentActiveConnections() {
        return activeConnections.get();
    }

    public int getPeakConnections() {
        return peakConnections.get(); // ⑦
    }

    private Optional<String> queryDatabase(String query) {
        try {
            Thread.sleep(new Random().nextInt(500) + 500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
        return Optional.of("Result for: " + query);
    }
}
