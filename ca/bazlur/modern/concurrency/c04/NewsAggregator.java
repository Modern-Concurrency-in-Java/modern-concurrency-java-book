package ca.bazlur.modern.concurrency.c04;

import module java.base;

import static ca.bazlur.modern.concurrency.c04.Utils.log;
import static java.util.concurrent.StructuredTaskScope.open;

public class NewsAggregator {
    public CollectingJoiner.Result<String> fetchAllHeadlines()
            throws InterruptedException {
        var newsSources = List.of(
                "TechCrunch",
                "InfoWorld",
                "InfoQ",
                "FailingSource"
        );

        try (var scope = open(new CollectingJoiner<String>())) {
            for (String source : newsSources) {
                scope.fork(() -> fetchHeadlines(source));              // ①
            }

            var result = scope.join();                               // ②

            log("Successfully fetched from " + result.successes().size() +
                    " sources");
            log("Failed to fetch from " + result.failures().size() +
                    " sources");

            return result;                                           // ③
        }
    }

    private String fetchHeadlines(String source) throws InterruptedException {
        log("Fetching headlines from " + source);
        Thread.sleep(Duration.ofMillis(200 + new Random().nextInt(300)));

        if (source.equals("FailingSource")) {                     // ④
            throw new RuntimeException("Network timeout for " + source);
        }

        return "Headlines from " + source + ": Breaking news, Tech updates";
    }

    void main() {
        var aggregator = new NewsAggregator();
        try {
            var result = aggregator.fetchAllHeadlines();

            log("\n=== Results ===");
            result.successes()                                       // ⑤
                    .forEach(headline -> log(headline));

            if (result.hasFailures()) {                             // ⑥
                log("\n=== Failures ===");
                result.failures()
                        .forEach(error ->
                                log(error.getMessage()));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log("Operation interrupted");
        }
    }
}
