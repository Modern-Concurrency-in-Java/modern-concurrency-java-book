package ca.bazlur.modern.concurrency.c04;

import module java.base;

public class ConditionalJoiner<T>
        implements StructuredTaskScope.Joiner<T, List<T>> {

    private final Supplier<Boolean> shouldContinue;                     // ①
    private final Queue<T> results = new ConcurrentLinkedQueue<>();
    private final Queue<Throwable> failures = new ConcurrentLinkedQueue<>();

    public ConditionalJoiner(Supplier<Boolean> shouldContinue) {
        this.shouldContinue = shouldContinue;                           // ②
    }

    @Override
    public boolean onFork(StructuredTaskScope.Subtask<? extends T> subtask) {
        if (!shouldContinue.get()) {                                    // ③
            System.out.println("Condition failed, stopping new tasks");
            return true;  // Cancel scope to prevent new work             // ④
        }

        return false;     // Condition satisfied, allow task           // ⑤
    }

    @Override
    public boolean onComplete(StructuredTaskScope.Subtask<? extends T> subtask) {
        switch (subtask.state()) {
            case SUCCESS -> results.add(subtask.get());                 // ⑥
            case FAILED -> failures.add(subtask.exception());           // ⑦
            case UNAVAILABLE -> failures.add(new RuntimeException("Task cancelled"));
        }

        return false;  // Continue processing existing tasks            // ⑧
    }

    @Override
    public List<T> result() {
        return results.stream().toList();
    }

    public List<Throwable> getFailures() {
        return failures.stream().toList();
    }
}
