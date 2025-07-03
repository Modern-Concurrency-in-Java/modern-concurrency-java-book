package ca.bazlur.modern.concurrency.c04;

import module java.base;

import static java.util.concurrent.StructuredTaskScope.*;

public class ComprehensiveConfigurationExample {

    public void main() {
        ThreadFactory factory = Thread.ofVirtual()    // ①
                .name("api-client-", 0)               // ②
                .factory();                   // ③

        Duration timeout = Duration.ofSeconds(15);  // ④

        try (var scope = open(Joiner.<String>allSuccessfulOrThrow(),
                cf -> cf.withThreadFactory(factory)      // ⑤
                        .withTimeout(timeout)          // ⑥
                        .withName("api-integration-scope"))) {   // ⑦

            System.out.println("Starting comprehensive API integration");
            System.out.println("Timeout: " + timeout.getSeconds() + " seconds");

            // Fork multiple API calls
            var userTask = scope.fork(this::callUserAPI);
            var profileTask = scope.fork(this::callProfileAPI);
            var preferencesTask = scope.fork(this::callPreferencesAPI);

            var results = scope.join()
                    .map(Subtask::get)
                    .toList();

            System.out.println("All API calls completed: " + results);

        } catch (StructuredTaskScope.TimeoutException e) {
            System.out.println("API integration timed out - some services too slow");

        } catch (StructuredTaskScope.FailedException e) {
            System.out.println("API integration failed: " + e.getCause().getMessage());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("API integration interrupted");
        }
    }

    private String callUserAPI() throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(3));
        IO.println(Thread.currentThread().getName()
                + " : " + "User data retrieved");
        return "User data";
    }

    private String callProfileAPI() throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(2));
        IO.println(Thread.currentThread().getName()
                + " : " + "Profile data retrieved");
        return "Profile data";
    }

    private String callPreferencesAPI() throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(4));
        IO.println(Thread.currentThread().getName()
                + " : " + "Preferences data retrieved");
        return "Preferences data";
    }
}
