package ca.bazlur.modern.concurrency.c04;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


// todo: potentially delete this file
//       added the updated fetchProduct and fetchReview methods here
// todo: the explanation numeration is out of order here because the individual methods appear
//       in different order than natural order in the class. These can be removed.
public class ProductServiceUpdated {

    private static void log(String message) {
        Thread currentThread = Thread.currentThread();
        String threadName = currentThread.isVirtual()
                ? "VThread[#" + currentThread.threadId() + "]"
                : currentThread.getName(); // ①
        String currentTime = LocalTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        System.out.printf("%s %-15s: %s%n", currentTime, threadName, message);
    }

    void main() { // ②
        ProductServiceUpdated productService = new ProductServiceUpdated();
        long testProductId = 1L;

        log("Attempting to fetch product info for ID: " + testProductId);
        try {
            ProductInfo productInfo = productService.fetchProductInfo(testProductId);
            log("Successfully retrieved: " + productInfo);
        } catch (ProductServiceException e) {
            log("Service Error: " + e.getMessage() +
                    (e.getCause() != null ? " | Caused by: " +
                            e.getCause().getMessage() : ""));
        }
    }

    public ProductInfo fetchProductInfo(Long productId) {
        log("Fetching product & reviews for id: " + productId);

        try (var executorService = Executors.newVirtualThreadPerTaskExecutor()) { // ①

            Future<Product> productTask = executorService.submit(() -> fetchProduct(productId)); // ②
            Future<List<Review>> reviewsTask = executorService.submit(() -> fetchReviews(productId)); // ③

            Product product = productTask.get(); // ④
            log("Product retrieved for id: " + productId);

            List<Review> reviews = reviewsTask.get(); // ⑤
            log("Reviews retrieved for id: " + productId);

            log("All info fetched for id: " + productId);
            return new ProductInfo(product, reviews); // ⑥

        } catch (ExecutionException | InterruptedException e) { // ⑦
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            log("Error processing product info for id: " +
                    productId + ": " + cause.getMessage());

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            throw new ProductServiceException(
                    "Fetch failed for id: " + productId, cause);
        }
    }

    private Product fetchProduct(Long productId) {
        log("Fetching product id: " + productId);
        if (productId == 1L) {
            log("Product id: " + productId
                    + " - simulating long network call (5 seconds).");
            sleepForAWhile(Duration.ofSeconds(5));
            log("Product id: " + productId + " fetch complete.");
            return new Product(productId,
                    "Long-Fetched Product", "This product takes time to fetch.");
        }

        log("Product id: " + productId +
                " - simulating standard network call (1 second).");
        sleepForAWhile(Duration.ofSeconds(1));
        log("Product id: " + productId + " fetch complete.");
        return new Product(productId, "Sample Product",
                "A great product description.");
    }

    private List<Review> fetchReviews(Long productId) {
        log("Fetching reviews for id: " + productId);
        if (productId == 1L) {
            log("Reviews for id: " + productId
                    + " - simulating quick failure after 1 second.");
            sleepForAWhile(Duration.ofSeconds(1));
            throw new ProductServiceException("Simulated failure " +
                    "fetching reviews for product " + productId);
        }

        log("Reviews for id: " + productId
                + " - simulating network call (2 seconds).");
        sleepForAWhile(Duration.ofSeconds(2));
        List<Review> reviews = List.of(
                new Review(1L, "Excellent!", 5, productId),
                new Review(2L, "Good value.", 4, productId));
        log("Fetched reviews for id: " + productId);
        return reviews;
    }

    private void sleepForAWhile(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted during sleep", e);
        }
    }

    record Product(Long id, String name, String description) {
    }

    record Review(Long id, String comment, int rating, Long productId) {
    }

    record ProductInfo(Product product, List<Review> reviews) {
    }

    class ProductServiceException extends RuntimeException {
        public ProductServiceException(String message, Throwable cause) {
            super(message, cause);
        }

        public ProductServiceException(String message) {
            super(message);
        }
    }

}
