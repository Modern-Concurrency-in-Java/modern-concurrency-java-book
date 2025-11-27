package ca.bazlur.modern.concurrency.c03;

import java.time.Duration;

public class Demo {

    public static void main(String[] args) throws Exception {
        FileOperation fileOperation = new FileOperation();
        for (int i = 0; i < 4; i++) {
            int finalI = i;
            NanoThread.start(() -> {
                System.out.println("Transfer: " + "File_" + finalI
                        + " Running in VThread: " + NanoThread.currentVThread());

                fileOperation.transfer("File_" + finalI);
                System.out.println("Transfer: " + "File_" + finalI
                        + " Completed in VThread: " + NanoThread.currentVThread());
            });
        }

        // Let's wait for a minute to allow the schedulers to run all our nano threads
        Thread.sleep(Duration.ofMinutes(1));
    }
}
