package ca.bazlur.modern.concurrency.c04;

import module java.base;

import static ca.bazlur.modern.concurrency.c04.Utils.log;
import static java.util.concurrent.StructuredTaskScope.open;

public class MemoryConsistencyDemo {

    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private String configuration = "default";

    public void demonstrateMemoryConsistency() throws InterruptedException {
        // Owner thread sets up a shared state before forking
        configuration = "production-config";                              // ①
        cache.put("database-url", "prod.example.com");          // ②
        cache.put("api-key", "secret-key-123");                 // ③

        log("Owner thread prepared: " + configuration);

        try (var scope = open(StructuredTaskScope.Joiner.
                <String>allSuccessfulOrThrow())) {

            // Fork subtasks that read owner thread's data
            var configTask = scope.fork(() -> {        // ④
                log("Subtask sees: " + configuration);              // ⑤
                return "Config: " + configuration;
            });

            var cacheTask = scope.fork(() -> {            // ⑥
                String url = cache.get("database-url");             // ⑦
                log("Subtask found URL: " + url);
                return "Connected to: " + url;
            });

            var results = scope.join()                    // ⑧
                    .map(StructuredTaskScope.Subtask::get)
                    .toList();

            log("Owner received: " + results);                      // ⑨
        }
    }

    public void demonstrateSubtaskUpdates() throws InterruptedException {

        var results = new ConcurrentHashMap<String, String>();    // ①
        var counter = new AtomicInteger(0);                    // ②

        log("Initial count: " + counter.get());

        try (var scope = open(StructuredTaskScope.Joiner.
                <String>allSuccessfulOrThrow())) {

            var worker1 = scope.fork(() -> {            // ③
                results.put("task1", "completed");                    // ④
                int count = counter.incrementAndGet();          // ⑤
                log("Worker1 incremented to: " + count);
                return "Worker1 done";
            });

            var worker2 = scope.fork(() -> {            // ⑥
                results.put("task2", "completed");                     // ⑦
                int count = counter.incrementAndGet();          // ⑧
                log("Worker2 incremented to: " + count);
                return "Worker2 done";
            });

            scope.join();                                                // ⑨

            // Owner thread sees all subtask updates
            log("Final count: " + counter.get());                    // ⑩
            log("Final results: " + results);                        // ⑪
        }
    }
}
