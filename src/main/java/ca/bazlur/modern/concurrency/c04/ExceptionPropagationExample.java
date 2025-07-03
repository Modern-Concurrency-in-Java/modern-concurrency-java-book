package ca.bazlur.modern.concurrency.c04;

import module java.base;

import static ca.bazlur.modern.concurrency.c04.Utils.log;
import static java.util.concurrent.StructuredTaskScope.FailedException;
import static java.util.concurrent.StructuredTaskScope.open;

public class ExceptionPropagationExample {

    // Let exceptions propagate for centralized handling
    public List<String> fetchCriticalData(List<String> sources)
            throws StructuredTaskScope.FailedException,
            InterruptedException { // ①

        try (var scope = open(StructuredTaskScope.Joiner.
                <String>allSuccessfulOrThrow())) {

            var tasks = sources.stream()
                    .map(source -> scope.fork(()
                            -> fetchFromSource(source)))
                    .toList();

            // If any source fails, let FailedException propagate
            var results = scope.join();

            return results.map(StructuredTaskScope.Subtask::get)
                    .toList();
        }
        // No catch block - let FailedException propagate // ②
    }

    // Higher-level method with centralized exception handling
    public void processDataWithCentralizedHandling() {
        try {
            var sources = List.of("source1", "source2");
            var data = fetchCriticalData(sources);
            log("Successfully fetched data: " + data);
        } catch (StructuredTaskScope.FailedException e) { // ③
            // Centralized logging and error handling
            log("Critical data fetch failed: " +
                    e.getCause().getMessage());

            // Could trigger alerts, fallback procedures, etc.
            handleCriticalSystemFailure(e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log("Operation was interrupted");
        }
    }

    private String fetchFromSource(String source) {
        return "Critical data from source";
    }

    private void handleCriticalSystemFailure(FailedException e) {
        log(e.getMessage());
    }
}
