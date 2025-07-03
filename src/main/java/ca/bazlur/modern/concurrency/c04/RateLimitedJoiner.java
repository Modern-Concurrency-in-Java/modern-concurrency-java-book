package ca.bazlur.modern.concurrency.c04;

import module java.base;

public class RateLimitedJoiner<T>
        implements StructuredTaskScope.Joiner<T, List<T>> {

    private final Semaphore semaphore;                                  // ①
    private final Queue<T> results = new ConcurrentLinkedQueue<>();
    private final Queue<Throwable> failures = new ConcurrentLinkedQueue<>();

    public RateLimitedJoiner(int maxConcurrentTasks) {
        this.semaphore = new Semaphore(maxConcurrentTasks);             // ②
    }

    @Override
    public boolean onFork(StructuredTaskScope.Subtask<? extends T> subtask) {
        try {
            semaphore.acquire();                                          // ③
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;                                                   // ④
    }

    @Override
    public boolean onComplete(StructuredTaskScope.Subtask<? extends T> subtask) {
        switch (subtask.state()) {
            case SUCCESS -> results.add(subtask.get());                   // ⑤
            case FAILED -> failures.add(subtask.exception());             // ⑥
            case UNAVAILABLE -> failures.add(new RuntimeException("Task cancelled"));
        }

        semaphore.release();       // ⑦
        return false;                  // ⑧
    }

    @Override
    public List<T> result() {
        return results.stream().toList();
    }
}
