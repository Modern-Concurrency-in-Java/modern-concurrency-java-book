package ca.bazlur.modern.concurrency.c04;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.StructuredTaskScope;

import static ca.bazlur.modern.concurrency.c04.Utils.log;

public class RacePolicyDemo {

    void main() {
        var demo = new RacePolicyDemo();
        log("--- Running Race Scenario ---");
        log("... Three tasks will race. " +
                "Expecting to finish in ~500ms (the fastest task)...\n");
        try {
            Product winningProduct = demo.fetchProduct(123L); // ①
            log("Race finished! Winning result: " + winningProduct);
        } catch (Exception e) {
            log("Caught unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Product fetchProduct(long productId) {
        Instant start = Instant.now();
        // The Joiner specifies the "race-to-win" policy
        try (var scope = StructuredTaskScope.open(
                StructuredTaskScope.Joiner.<Product>anySuccessfulResultOrThrow())) { // ①
            scope.fork(() -> fetchProductFromDatabase(productId)); // ②
            scope.fork(() -> fetchProductFromCache(productId)); // ③
            scope.fork(() -> fetchProductFromAPI(productId)); // ④
            // join() now returns the result of the first successful subtask
            return scope.join(); // ⑤
        } catch (InterruptedException | StructuredTaskScope.FailedException e) {
            // FailedException is thrown if ALL subtasks fail
            throw new RuntimeException(e); // ⑥
        } finally {
            Instant end = Instant.now();
            log("Total time taken: %dms%n"
                    .formatted(Duration.between(start, end).toMillis()));
        }
    }

    private Product fetchProductFromCache(long productId) throws InterruptedException {
        log(" -> Checking cache... (will take 500ms)");
        Thread.sleep(Duration.ofMillis(500)); // ①
        log(" <- Cache has the result!");
        return new Product(productId, "Product from Cache");
    }

    // A slower source (2000ms)
    private Product fetchProductFromDatabase(long productId)
            throws InterruptedException {
        try {
            log(" -> Querying database... (will take 2s)");
            Thread.sleep(Duration.ofSeconds(2)); // ②
            log(" <- Database has the result!");
            return new Product(productId, "Product from DB");
        } catch (InterruptedException e) {
            // This block will execute when the scope cancels this task
            log(" <- Database query was cancelled."); // ③
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    // The slowest source (3000ms)
    private Product fetchProductFromAPI(long productId)
            throws InterruptedException {
        try {
            log(" -> Calling external API... (will take 3s)");
            Thread.sleep(Duration.ofSeconds(3)); // ④
            log(" <- API has the result!");
            return new Product(productId, "Product from API");
        } catch (InterruptedException e) {
            // This block will also execute upon cancellation
            log(" <- API call was cancelled."); // ⑤
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    record Product(Long id, String name) {
    }

    record Review(Long id, String comment, int rating, Long productId) {
    }
}
