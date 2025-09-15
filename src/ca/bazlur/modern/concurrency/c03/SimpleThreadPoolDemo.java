package ca.bazlur.modern.concurrency.c03;

// todo: weird class name
public class SimpleThreadPoolDemo {

    public static void main(String[] args) throws InterruptedException {
        try (SimpleThreadPool simpleThreadPool
                 = new SimpleThreadPool(4, 100)) {
            for (int i = 0; i < 100; i++) {
                int finalI = i;
                simpleThreadPool.submit(() -> {
                    System.out.println("Task " + finalI + " is being executed by "
                        + Thread.currentThread().getName());
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }

        Thread.sleep(10_000);
        System.out.println("Main thread finished");
    }
}
