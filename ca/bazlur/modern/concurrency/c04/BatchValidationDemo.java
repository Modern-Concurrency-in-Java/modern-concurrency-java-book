package ca.bazlur.modern.concurrency.c04;

import module java.base;

import ca.bazlur.modern.concurrency.c04.model.ValidatedUser;

import java.util.concurrent.StructuredTaskScope.FailedException;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.concurrent.StructuredTaskScope.Subtask;

import static ca.bazlur.modern.concurrency.c04.Utils.log;
import static java.util.concurrent.StructuredTaskScope.open;

public class BatchValidationDemo {

    void main() {
        var demo = new BatchValidationDemo();
        List<Long> successfulBatch = List.of(1L, 2L, 4L, 5L);
        // Contains the failing ID '3'
        List<Long> failingBatch = List.of(1L, 2L, 3L, 4L, 5L); // ①

        log("--- Running Success Scenario ---");
        try {
            List<ValidatedUser> results = demo.validateAllUsers(successfulBatch); // ②
            log("\nBatch validation complete. Results:");
            results.forEach(validatedUser -> log(validatedUser.toString()));
        } catch (Exception e) {
            log("Caught unexpected exception: " + e.getMessage());
        }

        log("\n==============================================\n");
        log("--- Running Failure Scenario ---");
        try {
            demo.validateAllUsers(failingBatch); // ③
        } catch (Exception e) {
            log("Caught expected exception: " + e.getMessage());
        }
    }

    private ValidatedUser validateUser(long userId) throws InterruptedException {
        log(" -> Validating user %d...".formatted(userId));
        Thread.sleep(Duration.ofMillis(100 + new Random().nextInt(500))); // ①
        log(" <- User %d is valid.".formatted(userId));
        return new ValidatedUser(userId, "VALID");
    }

    private ValidatedUser validateUserWithFailure(long userId) throws InterruptedException {
        if (userId == 3L) {
            log(" -> Validating user %d... (will fail)".formatted(userId));
            throw new IllegalArgumentException("Invalid user ID: " + userId); // ②
        }
        return validateUser(userId);
    }

    public List<ValidatedUser> validateAllUsers(List<Long> userIds) throws InterruptedException {
        log("Validating a batch of " + userIds.size() + " users...");
        try (var scope = open(Joiner.<ValidatedUser>allSuccessfulOrThrow())) { // ①

            var subtasks = userIds.stream()
                    .map(id -> scope.fork(() -> validateUserWithFailure(id))) // ②
                    .toList();

            // join() returns a Stream<Subtask<ValidatedUser>> on success
            // or throws FailedException on the first failure
            var resultStream = scope.join(); // ③

            log("...All users validated successfully. Processing stream...");
            return resultStream
                    .map(Subtask::get) // ④
                    .toList();
        } catch (FailedException ex) {
            log("...Validation failed for one of the users.");
            throw new RuntimeException("Batch validation failed",
                    ex.getCause()); // ⑤
        }
    }
}
