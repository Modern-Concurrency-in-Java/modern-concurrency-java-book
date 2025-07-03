package ca.bazlur.modern.concurrency.c04;

import module java.base;

public class AdaptiveJoiner<T>
        implements StructuredTaskScope.Joiner<T, CollectingJoiner.Result<T>> {

    private final int minSampleSize;
    private final double maxFailureRate;
    private final Queue<T> successes = new ConcurrentLinkedQueue<>();
    private final Queue<Throwable> failures = new ConcurrentLinkedQueue<>();

    public AdaptiveJoiner(double maxFailureRate, int minSampleSize) {
        this.maxFailureRate = maxFailureRate;
        this.minSampleSize = minSampleSize;
    }

    @Override
    public CollectingJoiner.Result<T> result() {
        return new CollectingJoiner.Result<>(
                successes.stream().toList(),
                failures.stream().toList()
        );
    }

    @Override
    public boolean onComplete(StructuredTaskScope.Subtask<? extends T> subtask) {
        switch (subtask.state()) {
            case SUCCESS -> successes.add(subtask.get());       // ①
            case FAILED -> failures.add(subtask.exception());   // ②
            case UNAVAILABLE -> failures.add(
                    new RuntimeException("Task cancelled"));
        }

        int total = successes.size() + failures.size();

        // Only check failure rate after minimum sample size
        if (total >= minSampleSize) {                           // ③
            double failureRate = (double) failures.size() / total;

            return failureRate > maxFailureRate;        // ④
        }

        return false;  // Continue processing                   // ⑤
    }
}
