package ca.bazlur.modern.concurrency.c05;

public class ReentrantCodeExample {

    private static final ScopedValue<Integer> RECURSION_DEPTH = ScopedValue.newInstance();

    public static void main(String[] args) {
        ScopedValue.where(RECURSION_DEPTH, 0)
                .run(() -> recursiveMethod(100));
    }

    private static void recursiveMethod(int n) {
        int depth = RECURSION_DEPTH.get();
        System.out.println("Recursion depth: " + depth);

        if (n > 0) {
            ScopedValue.where(RECURSION_DEPTH, depth + 1)
                    .run(() -> recursiveMethod(n - 1));
        }
    }
}
