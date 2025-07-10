package ca.bazlur.modern.concurrency.c04;

import ca.bazlur.modern.concurrency.c04.exception.ProductServiceException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;

import static ca.bazlur.modern.concurrency.c04.Utils.log;

public class DefaultPolicyDemo {

    void main() {
        var demo = new DefaultPolicyDemo();

        log("--- Running Success Scenario ---");
        log("... Expecting to take ~2 seconds (the time of the slowest task)...\n");
        try {
            ProductInfo result = demo.fetchProductInfo(123L, false); // ①
            log("\nSuccess! Result: " + result);
        } catch (Exception e) {
            log("\nCaught unexpected exception in success scenario: " + e.getMessage());
        }

        // todo: decide whether merging success and failure scenario is intended
        log("--- Running Failure Scenario ---");
        log("... Expecting to fail almost instantly...\n");
        try {
            demo.fetchProductInfo(456L, true); // ①
        } catch (Exception e) {
            // We expect the RuntimeException thrown from our catch block
            log("\nCaught expected exception in failure scenario: " + e.getMessage());
            log("Cause: " + e.getCause()); // ②
        }

        /**
         * todo:
         * review how to integrate this case here
         * I wrote the following block to make an working example
         */
        log("--- Fetch product with traditional executor ---");
        try {
            demo.fetchProductInfoWithExecutor(456L);
        } catch (Exception e) {
            log("\nCaught expected exception in executor demo: " + e.getMessage());
            log("Cause: " + e.getCause());
        }
    }

    public ProductInfo fetchProductInfo(long productId, boolean shouldFail)
            throws InterruptedException {
        Instant start = Instant.now();
        // Using open() provides the default "fail-fast" policy
        try (var scope = StructuredTaskScope.open()) { // ①
            StructuredTaskScope.Subtask<Product> productTask = shouldFail
                    ? scope.fork(() -> fetchProductThatFails(productId)) // ②
                    : scope.fork(() -> fetchProduct(productId)); // ③

            StructuredTaskScope.Subtask<List<Review>> reviewsTask = scope.fork(() -> fetchReviews(productId)); // ④

            // Waits for both to succeed, or throws FailedException on first failure
            log("... Scope joining. Waiting for subtasks...");
            scope.join(); // ⑤
            log("... Scope joined successfully.");

            // Only reachable if join() succeeds
            return new ProductInfo(productTask.get(), reviewsTask.get()); // ⑥

        } catch (StructuredTaskScope.FailedException ex) {
            // This block executes only in the failure scenario
            log("... Scope join failed. A subtask threw an exception.");
            throw new RuntimeException("Failed to fetch product info", ex.getCause()); // ⑦
        } finally {
            Instant end = Instant.now();
            log("Total time taken: " + Duration.between(start, end).toMillis() + "ms");
        }
    }

    ProductInfo fetchProductInfoWithExecutor(Long productId)
            throws ExecutionException, InterruptedException {
        Instant start = Instant.now();
        try (ExecutorService service = Executors.newVirtualThreadPerTaskExecutor()) { // ①
            Future<Product> productFuture = service.submit(() -> fetchProductThatFails(productId)); // ②
            Future<List<Review>> reviewFuture = service.submit(() -> fetchReviews(productId)); // ③
            Product product = productFuture.get(); // ④
            List<Review> reviews = reviewFuture.get(); // ⑤
            return new ProductInfo(product, reviews);
        } finally {
            Instant end = Instant.now();
            System.out.printf("Time taken: %dms%n",
                    end.toEpochMilli() - start.toEpochMilli());
        }
    }

    // A subtask that succeeds after a 1-second delay
    private Product fetchProduct(long productId)
            throws InterruptedException {
        log(" -> Fetching product details... (will take 1s)");
        Thread.sleep(Duration.ofSeconds(1)); // ①
        log(" <- Product details fetched.");
        return new Product(productId, "Sample Product");
    }

    // A subtask that will always fail
    private Product fetchProductThatFails(long productId) {
        log(" -> Fetching product details... (will fail)");
        throw new ProductServiceException("Product ID " + productId + " not found"); // ②
    }

    // A subtask that succeeds after a 2-second delay
    private List<Review> fetchReviews(long productId)
            throws InterruptedException {
        log(" -> Fetching product reviews... (will take 2s)");
        Thread.sleep(Duration.ofSeconds(2)); // ③
        log(" <- Product reviews fetched.");
        return List.of(new Review("Inaya", 5), new Review("Rushda", 4));
    }

    record Product(long productId, String source) {
    }

    record Review(String user, int rating) {
    }

    record ProductInfo(Product product, List<Review> reviews) {
    }
}
