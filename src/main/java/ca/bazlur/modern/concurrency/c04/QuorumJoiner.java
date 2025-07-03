package ca.bazlur.modern.concurrency.c04;

import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.atomic.AtomicInteger;

public class QuorumJoiner<T> implements StructuredTaskScope.Joiner<T, Boolean> {

    private final int requiredSuccesses;
    private final AtomicInteger successCount = new AtomicInteger(0);        // ①
    private final AtomicInteger totalCount = new AtomicInteger(0);            // ②
    private volatile boolean quorumReached = false;                            // ③

    public QuorumJoiner(int requiredSuccesses) {
        this.requiredSuccesses = requiredSuccesses;
    }

    @Override
    public Boolean result() {
        return quorumReached;                                               // ④
    }

    @Override
    public boolean onFork(StructuredTaskScope.Subtask<? extends T> subtask) {
        totalCount.incrementAndGet();                                        // ⑤
        return false;  // Allow all tasks to proceed                        // ⑥
    }

    @Override
    public boolean onComplete(StructuredTaskScope.Subtask<? extends T> subtask) {
        if (subtask.state() == StructuredTaskScope.Subtask.State.SUCCESS) {
            int currentSuccess = successCount.incrementAndGet();              // ⑦

            if (currentSuccess >= requiredSuccesses) {                       // ⑧
                quorumReached = true;
                return true;  // ⑨
            }
        }
        return false;   // ⑩
    }

    public int getSuccessCount() {
        return successCount.get();
    }

    public int getTotalCount() {
        return totalCount.get();
    }
}
