package ca.bazlur.modern.concurrency.c02;

import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class PreventPinningExample {
    private static final ReentrantLock lock = new ReentrantLock(); // ①

    public static void main(String[] args) {
        var threadList = IntStream.range(0, 10)
                .mapToObj(i -> Thread.ofVirtual().unstarted(() -> {
                    if (i == 0) {
                        System.out.println(Thread.currentThread()); // ②
                    }
                    lock.lock(); // ③
                    try {
                        Thread.sleep(25); // ④
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        lock.unlock(); // ⑤
                    }
                    if (i == 0) {
                        System.out.println(Thread.currentThread()); // ⑥
                    }
                })).toList();

        threadList.forEach(Thread::start);
        threadList.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
