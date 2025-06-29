package ca.bazlur.modern.concurrency.c04;

import java.util.concurrent.StructuredTaskScope;

import static ca.bazlur.modern.concurrency.c04.Utils.log;
import static java.util.concurrent.StructuredTaskScope.open;

// todo: wrapper for methods in `Understanding How Different Joiners Handle Exceptions`
public class JoinerExceptionHandlerDemo {

    private void demonstrateAllSuccessfulOrThrow() {
        try (var scope = open(StructuredTaskScope.Joiner.
                <String>allSuccessfulOrThrow())) { // ①

            scope.fork(() -> successfulTask("Task1"));
            scope.fork(() -> failingTask("Task2")); // ②
            scope.fork(() -> successfulTask("Task3"));

            var results = scope.join(); // ③
            log("All tasks completed successfully");

        } catch (StructuredTaskScope.FailedException e) { // ④
            log("Failed due to: " + e.getCause().getMessage());
            log("Remaining tasks were cancelled");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void demonstrateAwaitAll() {
        try (var scope = open(StructuredTaskScope.Joiner.
                <Void>awaitAll())) { // ①

            scope.fork(() -> successfulTask("Task1"));
            scope.fork(() -> failingTask("Task2")); // ②
            scope.fork(() -> successfulTask("Task3"));

            scope.join(); // ③

            log("All tasks were allowed to complete");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void successfulTask(String task) {
    }

    private void failingTask(String task) {
    }
}
