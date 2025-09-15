package ca.bazlur.modern.concurrency.c03;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Playground {
  public static void main(String[] args) {
    try (ExecutorService fixedPool = Executors.newFixedThreadPool(4)) {
      for (int i = 0; i < 10; i++) {
        fixedPool.submit(() -> {
          System.out.println(Thread.currentThread().getName()
              + " is executing a task");
        });
      }
    }
  }
}
