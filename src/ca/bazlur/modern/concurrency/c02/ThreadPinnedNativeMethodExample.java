package ca.bazlur.modern.concurrency.c02;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

public class ThreadPinnedNativeMethodExample {

    public static void main(String[] args) {
        List<Thread> threadList = IntStream.range(0, 10)
                .mapToObj(i -> Thread.ofVirtual().unstarted(() -> {
                    if (i == 0) {
                        System.out.println(Thread.currentThread()); // ①
                    }
                    int sum = invokeNativeAddNumbers(56, 11); // ②
                    if (i == 0) {
                        System.out.println(Thread.currentThread()); // ③
                    }
                })).toList();

        threadList.forEach(Thread::start);
        threadList.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public static int invokeNativeAddNumbers(int a, int b) {
        try (Arena arena = Arena.ofConfined()) { // ④
            SymbolLookup lookup = SymbolLookup.libraryLookup(
                    Path.of("libaddNumbers.so"), arena); // ⑤

            MemorySegment memorySegment = lookup.find("addNumbers")
                    .orElseThrow(() -> new RuntimeException("addNumbers function not found"));

            Linker linker = Linker.nativeLinker();
            FunctionDescriptor addNumbersDescriptor = FunctionDescriptor.of(
                    ValueLayout.JAVA_INT, // return type
                    ValueLayout.JAVA_INT, // parameter 1
                    ValueLayout.JAVA_INT); // parameter 2

            MethodHandle addNumbersHandle = linker.downcallHandle(
                    memorySegment, addNumbersDescriptor); // ⑥

            try {
                return (int) addNumbersHandle.invokeExact(a, b); // ⑦
            } catch (Throwable e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }
}
