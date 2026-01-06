package ca.bazlur.modern.concurrency.c04;

import ca.bazlur.modern.concurrency.c04.exception.ProductServiceException;
import ca.bazlur.modern.concurrency.c04.model.Product;
import ca.bazlur.modern.concurrency.c04.model.ProductInfo;
import ca.bazlur.modern.concurrency.c04.model.Review;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;

public class FailingProductServiceWithStructuredConcurrency {

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
        FailingProductServiceWithStructuredConcurrency productService = new FailingProductServiceWithStructuredConcurrency();
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

        try (var scope = StructuredTaskScope.open()) {  // ①
            StructuredTaskScope.Subtask<Product> productTask =
                    scope.fork(() -> fetchProduct(productId));  // ②
            StructuredTaskScope.Subtask<List<Review>> reviewsTask =
                    scope.fork(() -> fetchReviews(productId));  // ③
            scope.join();  // ④

            return new ProductInfo(productTask.get(), reviewsTask.get());  // ⑤
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProductServiceException(
                    "Fetch failed for id: " + productId);  // ⑥
        }
    }

    private Product fetchProduct(Long productId) {
        log("Fetching product id: " + productId);

        if (productId == 1L) {
            throw new ProductServiceException("Product not found");  // ①
        }

        sleepForAWhile(Duration.ofSeconds(1)); // Simulate network call

        return new Product(productId, "Sample Product",
                "A great product description.");
    }

    private List<Review> fetchReviews(Long productId) {
        log("Fetching reviews for id: " + productId);
        sleepForAWhile(Duration.ofSeconds(2)); // Simulate network call
        return List.of(
                new Review(1L, "Excellent!", 5, productId),
                new Review(2L, "Good value.", 4, productId));
    }

    private void sleepForAWhile(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted during sleep", e);
        }
    }
}
