package ca.bazlur.modern.concurrency.c04;

import module java.base;

import static ca.bazlur.modern.concurrency.c04.Utils.log;
import static java.util.concurrent.StructuredTaskScope.*;

public class SubtaskExceptionHandling {

    public List<ServiceResponse> gatherOptionalData(List<String> services)
            throws InterruptedException {
        try (var scope = open(Joiner.<ServiceResponse>allSuccessfulOrThrow())) { // ①
            var tasks = services.stream()
                    .map(service -> scope.fork(() ->
                            fetchWithDefaults(service))) // ②
                    .toList();

            var results = scope.join();
            return results.map(Subtask::get)
                    .toList();

        } catch (FailedException e) { // ③
            // This should rarely happen since we handle
            // exceptions in subtasks
            log("Unexpected failure: " + e.getCause().getMessage());
            throw new RuntimeException("System error", e);
        }
    }

    // Handle exceptions within the subtask to provide defaults
    private ServiceResponse fetchWithDefaults(String service) { // ④
        try {
            String data = fetchServiceData(service);
            return new ServiceResponse(service, data, true);
        } catch (IOException e) {
            log("Network error for " + service + ": " + e.getMessage());
            return new ServiceResponse(service, "Default data", false);
        } catch (TimeoutException e) {
            log("Timeout for " + service + ": " + e.getMessage());
            return new ServiceResponse(service, "Cached data", false);
        } catch (Exception e) {
            log("Unexpected error for " + service + ": " +
                    e.getMessage());
            return new ServiceResponse(service, "Error", false);
        }
    }

    private String fetchServiceData(String service)
            throws IOException, TimeoutException, InterruptedException {
        Thread.sleep(Duration.ofMillis(100));
        return "Data from " + service;
    }

    public record ServiceResponse(String service, String data,
                                  boolean successful) {
    }
}
