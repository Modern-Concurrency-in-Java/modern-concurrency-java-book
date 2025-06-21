package ca.bazlur.modern.concurrency.c01;

public class ParallelExecutionDemo {

    public static void main(String[] args) throws Exception {
        CreditCalculatorService service = new CreditCalculatorService();

        System.out.println("=== Sequential Execution ===");
        ExecutionTimer.measure(() -> service.calculateCredit(1L));

        System.out.println("\n=== Parallel Execution ===");
        ExecutionTimer.measure(() -> {
            try {
                return service.calculateCreditWithUnboundedThreads(1L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });

        System.out.println("\n=== Parallel Execution With Executors ===");
        ExecutionTimer.measure(() -> {
            try {
                return service.calculateCreditWithExecutor(1L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });

        System.out.println("\n=== Parallel Execution With Virtual Threads ===");
        ExecutionTimer.measure(() -> {
            try {
                return service.calculateCreditWithVirtualThread(1L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });

        System.out.println("\n=== Performance Analysis ===");
        System.out.println("Sequential: ~1000ms (5 × 200ms)");
        System.out.println("Parallel: ~600ms (200ms + max(200ms, 200ms, 200ms) + 200ms)");
        System.out.println("Improvement: ~38% faster");
    }
}
