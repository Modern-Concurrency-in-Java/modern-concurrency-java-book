package ca.bazlur.modern.concurrency.c02;

public class VirtualThreadInterruption {
    public static void main(String[] args) {
        Thread virtualThread = Thread.ofVirtual().start(() -> { // ①
            try {
                System.out.println("Virtual thread started...");
                for (int i = 0; i < 5; i++) {
                    System.out.println("Virtual thread working: " + i);
                    Thread.sleep(1000); // Automatically yields to other virtual threads
                }
                System.out.println("Virtual thread finished.");
            } catch (InterruptedException e) { // ②
                System.out.println("Virtual thread interrupted!");
                // Handle cleanup if needed
            }
        });

        try {
            Thread.sleep(2500); // Let the thread run for a bit
        } catch (InterruptedException e) {
        }

        virtualThread.interrupt(); // ④
    }
}
