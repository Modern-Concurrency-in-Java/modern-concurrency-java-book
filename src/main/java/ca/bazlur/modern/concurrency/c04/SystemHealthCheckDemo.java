package ca.bazlur.modern.concurrency.c04;


import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.StructuredTaskScope.open;

public class SystemHealthCheckDemo {

  void main() throws InterruptedException {
    var results = new SystemHealthCheckDemo()
        .processWithSystemHealthCheck();
    results.forEach(System.out::println);
  }

  public List<String> processWithSystemHealthCheck()
      throws InterruptedException {
    var healthChecker = new SystemHealthChecker();
    var joiner = new ConditionalJoiner<String>(healthChecker::isSystemHealthy);

    try (var scope = open(joiner)) {
      for (int i = 0; i < 10; i++) {
        int id = i;
        scope.fork(() -> processTask("health-task-" + id));
      }
      return scope.join();
    }
  }

  private String processTask(String taskName)
      throws InterruptedException {
    Thread.sleep(Duration.ofMillis(200
        + new Random().nextInt(300)));

    if (taskName.contains("error-task")
        && new Random().nextDouble() < 0.4)
      throw new RuntimeException("Task failed: " + taskName);

    return "Completed: " + taskName;
  }

  private static class SystemHealthChecker {
    private final AtomicInteger checkCount
        = new AtomicInteger(0);

    public boolean isSystemHealthy() {
      checkCount.incrementAndGet();
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      return checkCount.get() < 7;
    }
  }
}