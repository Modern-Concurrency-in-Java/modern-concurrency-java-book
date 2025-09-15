package ca.bazlur.modern.concurrency.c02;

import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JFRVirtualThreadDemo {

    private static final Object syncLock = new Object();
    private static final Lock reentrantLock = new ReentrantLock();

    public static void main(String[] args) {
        // Triggering lifecycle events for virtual threads
        Thread vThreadStartEnd = Thread.ofVirtual().unstarted(() -> {
            System.out.println("Virtual thread started and will end soon.");
        }); // ①
        vThreadStartEnd.start();
        joinThread(vThreadStartEnd);

        // Pinning with a synchronized block
        Thread vThreadPinnedSync = Thread.ofVirtual().unstarted(() -> {
            synchronized (syncLock) { // ②
                sleepUninterruptibly(Duration.ofMillis(500));
            }
        });
        vThreadPinnedSync.start();
        joinThread(vThreadPinnedSync);

        // No pinning with ReentrantLock
        Thread vThreadWithLock = Thread.ofVirtual().unstarted(() -> {
            reentrantLock.lock();
            try {
                sleepUninterruptibly(Duration.ofMillis(500)); // ③
            } finally {
                reentrantLock.unlock();
            }
        });
        vThreadWithLock.start();
        joinThread(vThreadWithLock);
    }

    private static void joinThread(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void sleepUninterruptibly(Duration duration) { // ④
        boolean interrupted = false;
        try {
            long remainingNanos = duration.toNanos();
            long end = System.nanoTime() + remainingNanos;

            while (true) {
                try {
                    Thread.sleep(remainingNanos / 1_000_000,
                            (int) (remainingNanos % 1_000_000));
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                    remainingNanos = end - System.nanoTime();
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
