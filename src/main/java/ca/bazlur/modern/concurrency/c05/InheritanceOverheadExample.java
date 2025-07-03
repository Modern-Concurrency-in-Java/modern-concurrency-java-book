package ca.bazlur.modern.concurrency.c05;

public class InheritanceOverheadExample {
    private static final InheritableThreadLocal<byte[]> LARGE_DATA =
            new InheritableThreadLocal<>(); //①

    public static void main(String[] args) {
        // Parent thread sets a large object
        LARGE_DATA.set(new byte[10_000_000]); // 10MB ②

        // Create multiple child threads
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                // Each child thread gets a reference to the parent's data ③
                byte[] inherited = LARGE_DATA.get();
                System.out.println("Child has access to " +
                        inherited.length + " bytes");
            }).start();
        }
    }
}
