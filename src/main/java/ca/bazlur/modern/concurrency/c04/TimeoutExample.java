package ca.bazlur.modern.concurrency.c04;

import module java.base;

import static java.util.concurrent.StructuredTaskScope.*;

public class TimeoutExample {

    public List<String> fetchDataWithTimeout(List<String> sources)
            throws TimeoutException,
            FailedException,
            InterruptedException {

        Duration timeout = Duration.ofSeconds(5); // ①

        try (var scope = open(Joiner.<String>allSuccessfulOrThrow(),
                cf -> cf.withTimeout(timeout))) { // ②

            var tasks = sources.stream()
                    .map(source -> scope.fork(() -> fetchFromSource(source)))
                    .toList();

            // If timeout expires before join() completes,
            // TimeoutException is thrown
            var results = scope.join() // ③
                    .map(Subtask::get)
                    .toList();

            return results;
        }
    }

    private String fetchFromSource(String source) throws InterruptedException {
        int delay = switch (source) {
            case "fast_source" -> 1000;
            case "slow_source" -> 3000;
            case "very_slow_source" -> 8000; // ④
            default -> 2000;
        };

        Thread.sleep(Duration.ofMillis(delay));
        return "Data from " + source;
    }

    void main() {
        var sources = List.of("fast_source", "slow_source", "very_slow_source");

        try {
            var results = fetchDataWithTimeout(sources);
            System.out.println("Success: " + results);
        } catch (TimeoutException e) { // ⑤
            System.out.println("Operation timed out after 5 seconds");
            System.out.println("Some sources were too slow to respond");
        } catch (FailedException e) {
            System.out.println("Task failed: " + e.getCause().getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Operation interrupted");
        }
    }
}
