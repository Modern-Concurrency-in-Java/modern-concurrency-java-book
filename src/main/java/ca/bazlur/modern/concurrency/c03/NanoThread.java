package ca.bazlur.modern.concurrency.c03;

import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;
import java.util.concurrent.atomic.AtomicInteger;

public class NanoThread {

    public static final NanoThreadScheduler NANO_THREAD_SCHEDULER
                            = new NanoThreadScheduler();
    private static final AtomicInteger COUNTER
                            = new AtomicInteger(1);
    public static final ContinuationScope SCOPE
                            = new ContinuationScope("nanoThreadScope");

    private final Continuation continuation;
    private final int nid;

    private NanoThread(Runnable runnable) {
        this.nid = COUNTER.getAndIncrement();
        this.continuation = new Continuation(SCOPE, runnable);
    }

    public static void start(Runnable runnable) {
        var nanoThread = new NanoThread(runnable);
        NANO_THREAD_SCHEDULER.schedule(nanoThread);
    }

    public void run() {
        continuation.run();
    }

    public static NanoThread currentVThread() {
        return NanoThreadScheduler.CURRENT_NANO_THREAD.get();
    }

    @Override
    public String toString() {
        return "NanoThread-" + nid + "-" + Thread.currentThread().getName();
    }
}
