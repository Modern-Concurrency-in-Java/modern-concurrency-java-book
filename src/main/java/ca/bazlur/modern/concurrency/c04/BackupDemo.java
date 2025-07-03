package ca.bazlur.modern.concurrency.c04;

import module java.base;

import static ca.bazlur.modern.concurrency.c04.Utils.log;
import static java.util.concurrent.StructuredTaskScope.Joiner;
import static java.util.concurrent.StructuredTaskScope.open;

public class BackupDemo {
    private final AtomicBoolean hasSuccess = new AtomicBoolean(false);

    void main() {
        try {
            performBackup("important-data.zip");
        } catch (Exception e) {
            log("Error: " + e.getMessage());
        }
    }

    public void performBackup(String data) throws InterruptedException {
        log("Starting backup to multiple locations...");

        try (var scope = open(Joiner.<BackupResult>allUntil(subtask -> {
            boolean shouldStop = hasSuccess.get(); // ①
            if (shouldStop) {
                log("✅ Backup successful! Cancelling other attempts...");
            }
            return shouldStop;
        }))) {

            scope.fork(() -> backupToCloud(data)); // ②
            scope.fork(() -> backupToUSB(data)); // ③
            scope.fork(() -> backupToNetwork(data)); // ④

            scope.join(); // ⑤

            if (hasSuccess.get()) {
                log("Backup completed successfully!");
            } else {
                log("All backup attempts failed!");
            }
        }
    }

    private BackupResult backupToCloud(String data) throws InterruptedException {
        log(" -> Backing up to cloud...");
        Thread.sleep(Duration.ofMillis(500));

        if (new Random().nextBoolean()) { // ①
            log(" <- Cloud backup successful");
            hasSuccess.set(true);
            return new BackupResult("Cloud", true);
        } else {
            log(" <- Cloud backup failed");
            return new BackupResult("Cloud", false);
        }
    }

    private BackupResult backupToUSB(String data) throws InterruptedException {
        log(" -> Backing up to USB...");
        Thread.sleep(Duration.ofMillis(300));

        if (new Random().nextBoolean()) { // ②
            log(" <- USB backup successful");
            hasSuccess.set(true);
            return new BackupResult("USB", true);
        } else {
            log(" <- USB backup failed");
            return new BackupResult("USB", false);
        }
    }

    private BackupResult backupToNetwork(String data) throws InterruptedException {
        log(" -> Backing up to network drive...");
        Thread.sleep(Duration.ofMillis(400));

        if (new Random().nextBoolean()) { // ③
            log(" <- Network backup successful");
            hasSuccess.set(true);
            return new BackupResult("Network", true);
        } else {
            log(" <- Network backup failed");
            return new BackupResult("Network", false);
        }
    }

    public record BackupResult(String location, boolean success) {
    }
}
