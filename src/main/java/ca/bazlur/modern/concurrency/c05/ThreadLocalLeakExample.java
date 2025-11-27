package ca.bazlur.modern.concurrency.c05;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadLocalLeakExample {
    private static final ThreadLocal<String> currentUser = new ThreadLocal<>();

    public static void main(String[] args) throws InterruptedException {
        try (ExecutorService executor = Executors.newFixedThreadPool(1)) {//①

            // The first task sets the current user
            executor.submit(() -> {
                currentUser.set("Alice"); //②
                System.out.println("Task 1: currentUser = " + currentUser.get());
                // Forgot to call currentUser.remove()! //③
            });

            Thread.sleep(100); // Ensure task 1 completes

            // The second task reuses the same thread
            executor.submit(() -> {
                System.out.println("Task 2: Leaked value = " + currentUser.get()); //④
                currentUser.set("Bob");
                System.out.println("Task 2: currentUser = " + currentUser.get());
                currentUser.remove(); //
            });
        }
    }
}
