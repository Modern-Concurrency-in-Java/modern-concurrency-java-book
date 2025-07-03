package ca.bazlur.modern.concurrency.c03;

import java.util.concurrent.ForkJoinPool;

public class AsyncModeExample {

    public static void main(String[] args) {
        try (ForkJoinPool forkJoinPool = new ForkJoinPool(4,
                ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true)) {
            for (int i = 0; i < 10; i++) {
                forkJoinPool.submit(new EventTask("Event " + i));
            }
        }
    }

    record EventTask(String eventName) implements Runnable {
        public void run() {
            System.out.println("Processing " + eventName + " in thread: " + Thread.currentThread().getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("Completed " + eventName + " in thread: " + Thread.currentThread().getName());
        }
    }
}
