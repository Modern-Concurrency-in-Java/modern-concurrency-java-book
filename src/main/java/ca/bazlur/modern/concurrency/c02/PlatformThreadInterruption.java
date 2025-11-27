package ca.bazlur.modern.concurrency.c02;

public class PlatformThreadInterruption {

    public static void main(String[] args) {
        Thread platformThread = Thread.ofPlatform().start(() -> { // ①
            try {
                System.out.println("Platform thread started...");
                for (int i = 0; i < 5; i++) {
                    System.out.println("Platform thread working: " + i);
                    Thread.sleep(1000); // Simulate work
                }
                System.out.println("Platform thread finished.");
            } catch (InterruptedException e) { // ②
                System.out.println("Platform thread interrupted!");
                // Handle cleanup if needed
            }
        });

        try {
            Thread.sleep(2500); // Let the thread run for a bit // ③
        } catch (InterruptedException e) {
        }

        platformThread.interrupt(); // ④
    }
}
