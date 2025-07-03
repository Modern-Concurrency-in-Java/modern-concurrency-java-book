package ca.bazlur.modern.concurrency.c04;

import module java.base;

import static ca.bazlur.modern.concurrency.c04.Utils.log;
import static java.util.concurrent.StructuredTaskScope.Joiner;
import static java.util.concurrent.StructuredTaskScope.open;

public class AwaitAllDemo {

    private final List<NotificationResult> notificationResults = new CopyOnWriteArrayList<>();
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);

    void main() {
        String criticalAlert = "URGENT: Database connection pool exhausted - " +
                "immediate attention required";
        log("--- Running Notification Scenario (awaitAll Policy) ---");
        log("This demonstrates how awaitAll() processes " +
                "ALL tasks regardless of failures\n");

        try {
            sendCriticalAlert(criticalAlert); // ①
        } catch (Exception e) {
            log("Caught exception: " + e.getMessage());
        }

        log("\n===============================\n");
        log("--- Running Second Notification Batch ---");

        try {
            sendCriticalAlert(
                    "RESOLVED: Database issue fixed - " +
                            "all systems operational"); // ②
        } catch (Exception e) {
            log("Caught exception: " + e.getMessage());
        }
    }

    public void sendCriticalAlert(String alertMessage) throws InterruptedException {
        log("Sending critical alert: " + alertMessage);

        try (var scope = open(Joiner.<Void>awaitAll())) { // ①
            // Fork notification tasks - each performs side effects
            scope.fork(() -> {
                sendEmailNotification(alertMessage);
                return null; // awaitAll() ignores return values // ②
            });

            scope.fork(() -> {
                sendSmsNotification(alertMessage);
                return null;
            });

            scope.fork(() -> {
                sendPushNotification(alertMessage);
                return null;
            });

            log("...Waiting for all notification attempts to complete...");
            // join() always returns null for awaitAll()
            // All tasks complete regardless of individual failures
            Void result = scope.join(); // ③

            log("...All notification attempts completed.");

            // Process the side effects (collected results)
            logNotificationSummary(); // ④

        } catch (InterruptedException e) {
            log("...Notification sending was interrupted");
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    private void sendEmailNotification(String message)
            throws InterruptedException {
        log(" -> Sending email notification...");
        Thread.sleep(Duration.ofMillis(200 + new Random().nextInt(300)));

        // Email is generally reliable (90% success rate)
        if (new Random().nextDouble() < 0.9) { // ①
            log(" <- Email sent successfully");
            notificationResults.add(
                    NotificationResult.success("EMAIL", "Delivered to inbox"));
            successCount.incrementAndGet();
        } else {
            log(" <- Email failed: SMTP server unavailable");
            notificationResults.add(
                    NotificationResult.failure("EMAIL", "SMTP server unavailable"));
            failureCount.incrementAndGet();
            throw new RuntimeException("Email delivery failed"); // ②
        }
    }

    private void sendSmsNotification(String message)
            throws InterruptedException {
        log(" -> Sending SMS notification...");
        Thread.sleep(Duration.ofMillis(150 + new Random().nextInt(400)));

        // SMS is less reliable (70% success rate)
        if (new Random().nextDouble() < 0.7) { // ③
            log(" <- SMS sent successfully");
            notificationResults.add(
                    NotificationResult.success("SMS", "Delivered to mobile"));
            successCount.incrementAndGet();
        } else {
            log(" <- SMS failed: Carrier gateway timeout");
            notificationResults.add(
                    NotificationResult.failure("SMS", "Carrier gateway timeout"));
            failureCount.incrementAndGet();
            throw new RuntimeException("SMS delivery failed");
        }
    }

    private void sendPushNotification(String message)
            throws InterruptedException {
        log(" -> Sending push notification...");
        Thread.sleep(Duration.ofMillis(100 + new Random().nextInt(200)));

        // Push notifications are most reliable (95% success rate)
        if (new Random().nextDouble() < 0.95) { // ④
            log(" <- Push notification sent successfully");
            notificationResults.add(
                    NotificationResult.success("PUSH", "Delivered to device"));
            successCount.incrementAndGet();
        } else {
            log(" <- Push notification failed: Device token expired");
            notificationResults.add(
                    NotificationResult.failure("PUSH", "Device token expired"));
            failureCount.incrementAndGet();
            throw new RuntimeException("Push notification delivery failed");
        }
    }

    private void logNotificationSummary() {
        log("\n--- Notification Summary ---");
        log("Total channels attempted: " + (successCount.get() + failureCount.get()));
        log("Successful deliveries: " + successCount.get());
        log("Failed deliveries: " + failureCount.get());

        log("\nDetailed results:");
        notificationResults.forEach(result -> {
            String status = result.success() ? "✅" : "❌";
            log(status + " " + result.channel() + ": " + result.message());
        });

        if (successCount.get() > 0) {
            log("\n🎯 Alert successfully delivered through " +
                    successCount.get() + " channel(s)");
        } else {
            log("\n⚠️  Alert failed to deliver through any channel!");
        }
    }

    public record NotificationResult(String channel, boolean success, String message) {
        public static NotificationResult success(String channel, String message) {
            return new NotificationResult(channel, true, message);
        }

        public static NotificationResult failure(String channel, String error) {
            return new NotificationResult(channel, false, error);
        }
    }
}
