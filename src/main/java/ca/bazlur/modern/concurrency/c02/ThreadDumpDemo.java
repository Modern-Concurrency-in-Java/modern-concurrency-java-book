package ca.bazlur.modern.concurrency.c02;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessHandle;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import com.sun.management.HotSpotDiagnosticMXBean;

public class ThreadDumpDemo {

    private static final int THREAD_COUNT = 1_000;
    private static final Duration WORK_DURATION = Duration.ofSeconds(5);
    private static final Duration DELAY_BEFORE_DUMP = Duration.ofSeconds(2);

    public static void main(String[] args) {
        long pid = ProcessHandle.current().pid(); // ①
        String outputFile = "dump.json";

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            IntStream.range(0, THREAD_COUNT).forEach(i -> executor.submit(() -> sleep(WORK_DURATION))); // ②

            executor.submit(() -> {
                sleep(DELAY_BEFORE_DUMP); // ③
                runJcmdDump(pid, outputFile);
            });
        }

        takeThreadDump("threadDump.json"); // provide an absolute path here
    }

    public static void takeThreadDump(String outputFile) {
        var hotSpotDiagnosticMXBean = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
        try {
            // Ensure that the output file path is absolute
            if (!new File(outputFile).isAbsolute()) {
                throw new IllegalArgumentException("The output file path must be absolute.");
            }
            hotSpotDiagnosticMXBean.dumpThreads(outputFile, HotSpotDiagnosticMXBean.ThreadDumpFormat.JSON);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while taking thread dump", e);
        }
    }

    private static void sleep(Duration d) {
        try {
            TimeUnit.NANOSECONDS.sleep(d.toNanos());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void runJcmdDump(long pid, String file) {
        ProcessBuilder pb = new ProcessBuilder(List.of(
                "/bin/sh", "-c",
                String.format("jcmd %d Thread.dump_to_file -format=json %s", pid, file))); // ④
        try {
            Process p = pb.start();
            int exit = p.waitFor();
            if (exit != 0) {
                System.err.printf("jcmd exited %d%n", exit);
                p.getInputStream().transferTo(System.err);
                p.getErrorStream().transferTo(System.err);
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Failed to run jcmd: " + e.getMessage());
        }
    }
}
