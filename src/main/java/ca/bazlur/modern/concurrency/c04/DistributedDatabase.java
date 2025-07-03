package ca.bazlur.modern.concurrency.c04;

import java.time.Duration;
import java.util.List;
import java.util.Random;

import static ca.bazlur.modern.concurrency.c04.Utils.log;
import static java.util.concurrent.StructuredTaskScope.open;

public class DistributedDatabase {

    private final List<String> nodes = List.of(
            "node-1", "node-2", "node-3", "node-4", "node-5"  // ①
    );
    private final int quorumSize = 3; // Need 3 out of 5 nodes  // ②

    public boolean writeData(String key, String value)
            throws InterruptedException {
        log("Writing data to distributed database: " + key + "=" + value);

        try (var scope = open(new QuorumJoiner<Boolean>(quorumSize))) {  // ③

            for (String node : nodes) {
                scope.fork(() -> writeToNode(node, key, value));  // ④
            }

            boolean success = scope.join();  // ⑤

            log("Write operation " + (success ? "succeeded" : "failed"));
            log("Required quorum: " + quorumSize + " nodes");

            return success;
        }
    }

    private Boolean writeToNode(String node, String key, String value)
            throws InterruptedException {
        log("Writing to " + node);
        Thread.sleep(Duration.ofMillis(100 + new Random().nextInt(200)));  // ⑥
        // Simulate occasional node failures
        if (new Random().nextDouble() < 0.2) {  //⑦

            log(node + " write failed");
            throw new RuntimeException("Write failed on " + node);
        }

        log(node + " write succeeded");
        return true;
    }

    void main() {
        var database = new DistributedDatabase();

        try {
            boolean result = database.writeData("user:123", "John Doe");  // ⑧
            log("\nFinal result: " + (result ? "SUCCESS" : "FAILURE"));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log("Database operation interrupted");
        }
    }
}
