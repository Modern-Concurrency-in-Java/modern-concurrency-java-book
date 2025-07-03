package ca.bazlur.modern.concurrency.c03;

import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;

// --add-exports java.base/jdk.internal.vm=ALL-UNNAMED
public class ContinuationExample {

    public static void main(String[] args) {
        ContinuationScope scope = new ContinuationScope("main");

        Continuation continuation = new Continuation(scope, () -> {
            System.out.println("Hello from continuation");
            Continuation.yield(scope);
            System.out.println("Hello again from continuation");
            Continuation.yield(scope);
            System.out.println("Done from continuation");
        });

        System.out.println("Before starting continuation");
        continuation.run();
        System.out.println("After starting continuation");
        continuation.run();
        System.out.println("After starting continuation again");
        continuation.run();
    }
}
