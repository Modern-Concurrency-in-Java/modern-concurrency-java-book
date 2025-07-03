package ca.bazlur.modern.concurrency.c04;

import module java.base;

import static java.util.concurrent.StructuredTaskScope.*;

public class NamedThreadExample {

    public void processUserRequests(List<String> userIds) {
        ThreadFactory factory = Thread.ofVirtual()
                .name("user-processor-", 0)
                .factory();

        try (var scope = open(Joiner.<String>allSuccessfulOrThrow(),
                cf -> cf.withThreadFactory(factory))) {

            var tasks = userIds.stream()
                    .map(userId -> scope.fork(() -> processUser(userId)))
                    .toList();

            var results = scope.join()
                    .map(Subtask::get)
                    .toList();

            System.out.println("Processed users: " + results);

        } catch (FailedException | InterruptedException e) {
            System.out.println("Processing failed: " + e.getMessage());
        }
    }

    private String processUser(String userId)
            throws InterruptedException {
        System.out.println("Processing user " + userId +
                " on thread: " + Thread.currentThread().getName());

        Thread.sleep(Duration.ofMillis(100));
        return "User " + userId + " processed";
    }

    void main() {
        processUserRequests(List.of("user1", "user2", "user3"));
    }
}
