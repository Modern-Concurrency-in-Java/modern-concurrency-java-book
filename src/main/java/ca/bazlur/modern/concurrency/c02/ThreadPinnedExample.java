package ca.bazlur.modern.concurrency.c02;

import java.util.List;
import java.util.stream.IntStream;

public class ThreadPinnedExample {

    private static final Object lock = new Object();

    public static void main(String[] args) {
        List<Thread> threadList = IntStream.range(0, 10)
                .mapToObj(i -> Thread.ofVirtual().unstarted(() -> {
                    if (i == 0) {
                        System.out.println(Thread.currentThread()); // ①
                    }
                    synchronized (lock) { // ②
                        try {
                            Thread.sleep(25); // ③
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    if (i == 0) {
                        System.out.println(Thread.currentThread()); // ④
                    }
                })).toList();

        threadList.forEach(Thread::start);
        threadList.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
