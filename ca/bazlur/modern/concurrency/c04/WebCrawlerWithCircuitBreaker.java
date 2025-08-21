package ca.bazlur.modern.concurrency.c04;

import module java.base;

import static ca.bazlur.modern.concurrency.c04.Utils.log;
import static java.util.concurrent.StructuredTaskScope.open;

public class WebCrawlerWithCircuitBreaker {

    private final List<String> urls = List.of(
            "https://api.service1.com/data",     // ①
            "https://api.service2.com/data",
            "https://api.service3.com/data",
            "https://api.service4.com/data",
            "https://api.service5.com/data"
    );

    private double systemFailureRate = 0.1; // Start with 10% failure rate
    private final Random random = new Random();

    void main() {
        var crawler = new WebCrawlerWithCircuitBreaker();

        try {
            crawler.demonstrateScenarios();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log("Crawling interrupted");
        }
    }

    public CollectingJoiner.Result<String> crawlWithCircuitBreaker()
            throws InterruptedException {
        log("Starting web crawl with circuit breaker protection");
        log("Max failure rate: 30%, Min sample size: 5");

        // Create adaptive joiner that stops if >30% fail after 5 samples
        try (var scope = open(new AdaptiveJoiner<String>(0.30, 5))) {  // ②

            for (String url : urls) {
                scope.fork(() -> fetchUrl(url));                        // ③
            }

            var result = scope.join();                                  // ④

            log("\n=== FINAL RESULTS ===");
            log("Successful fetches: " + result.successes().size());
            log("Failed fetches: " + result.failures().size());
            log("Total processed: " + (result.successes().size() + result.failures().size()));
            log("Remaining URLs (not processed): " +
                    (urls.size() - result.successes().size() - result.failures().size()));

            return result;
        }
    }

    private String fetchUrl(String url) throws InterruptedException {
        log("Fetching: " + url);

        // Simulate network delay
        Thread.sleep(Duration.ofMillis(200 + random.nextInt(300)));     // ⑤

        // Simulate system degradation over time
        if (random.nextDouble() < systemFailureRate) {                 // ⑥
            systemFailureRate += 0.05; // Failures increase over time
            throw new RuntimeException("Network timeout for " + url);
        }

        log("✓ Successfully fetched: " + url);
        return "Data from " + url;
    }

    public void demonstrateScenarios() throws InterruptedException {
        log("=== SCENARIO 1: Normal Operation ===");
        systemFailureRate = 0.1; // Low failure rate
        crawlWithCircuitBreaker();

        Thread.sleep(1000);

        log("\n=== SCENARIO 2: System Under Stress ===");
        systemFailureRate = 0.4; // High initial failure rate
        crawlWithCircuitBreaker();

        Thread.sleep(1000);

        log("\n=== SCENARIO 3: Cascading Failures ===");
        systemFailureRate = 0.6; // Very high failure rate
        crawlWithCircuitBreaker();
    }
}
