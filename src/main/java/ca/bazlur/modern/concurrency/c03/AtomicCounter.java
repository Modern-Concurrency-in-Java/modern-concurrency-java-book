package ca.bazlur.modern.concurrency.c03;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class AtomicCounter {
    private volatile int counter = 0;
    private static final VarHandle COUNTER_HANDLE;

    static {
        try {
            COUNTER_HANDLE = MethodHandles.lookup().findVarHandle(
                    AtomicCounter.class, "counter", int.class); // ①
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    public void increment() {
        int current;
        int next;
        do {
            current = counter; // ②
            next = current + 1;
        } while (!COUNTER_HANDLE.compareAndSet(this, current, next)); // ③
    }

    public int get() {
        return counter;
    }

    public static void main(String[] args) throws InterruptedException {
        AtomicCounter atomicCounter = new AtomicCounter();

        Thread.ofPlatform().start(() -> {
            for (int i = 0; i < 100; i++) {
                atomicCounter.increment();
            }
        });

        Thread.ofPlatform().start(() -> {
            for (int i = 0; i < 100; i++) {
                atomicCounter.increment();
            }
        });

        Thread.sleep(100);
        System.out.println("Final Counter Value: " + atomicCounter.get()); // ④
    }
}
