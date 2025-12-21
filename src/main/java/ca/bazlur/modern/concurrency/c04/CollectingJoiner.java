package ca.bazlur.modern.concurrency.c04;

import module java.base;

public class CollectingJoiner<T> implements StructuredTaskScope.Joiner<T, CollectingJoiner.Result<T>> {

    private final Queue<T> results = new ConcurrentLinkedQueue<>();
    private final Queue<Throwable> exceptions = new ConcurrentLinkedQueue<>();

    @Override
    public Result<T> result() {
        return new Result<>(
                results.stream().toList(),
                exceptions.stream().toList()
        );
    }

    @Override
    public boolean onComplete(StructuredTaskScope.Subtask<? extends T> subtask) {
        switch (subtask.state()) {
            case SUCCESS -> results.add(subtask.get());                    // ①
            case FAILED -> exceptions.add(subtask.exception());        // ②
            case UNAVAILABLE -> {                                                // ③
                // Task was cancelled, treat as failure
                exceptions.add(new RuntimeException("Task was cancelled"));
            }
        }
        return false;                                                // ④
    }

    public record Result<T>(List<T> successes, List<Throwable> failures) {
        public boolean hasFailures() {
            return !failures.isEmpty();
        }

        public int totalTasks() {
            return successes.size() + failures.size();
        }
    }
}
