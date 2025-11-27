package ca.bazlur.modern.concurrency.c04;

import module java.base;

import static ca.bazlur.modern.concurrency.c04.Utils.log;
import static java.util.concurrent.StructuredTaskScope.open;

public class RateLimitedAPIService {

    public List<String> fetchDataWithRateLimit() throws InterruptedException {
        var endpoints = List.of(
                "api/users", "api/orders", "api/products",
                "api/analytics", "api/reports", "api/logs"
        );

        // Allow maximum 3 concurrent API calls
        var rateLimitedJoiner = new RateLimitedJoiner<String>(3);

        try (var scope = open(rateLimitedJoiner)) {
            for (String endpoint : endpoints) {
                scope.fork(() -> callAPI(endpoint));                    // ①
            }

            var results = scope.join();                                 // ②

            log("Completed " + results.size() +
                    " API calls with rate limiting");
            return results;
        }
    }

    private String callAPI(String endpoint) throws InterruptedException {
        log("Starting API call: " + endpoint);
        Thread.sleep(Duration.ofMillis(500 + new Random().nextInt(1000)));
        log("Completed API call: " + endpoint);
        return "Response from " + endpoint;
    }

    void main() {
        var service = new RateLimitedAPIService();
        try {
            var results = service.fetchDataWithRateLimit();
            log("All results: " + results);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log("Service interrupted");
        }
    }
}
