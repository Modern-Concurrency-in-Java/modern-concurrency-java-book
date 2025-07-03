package ca.bazlur.modern.concurrency.c04;

import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;

import static ca.bazlur.modern.concurrency.c04.Utils.log;
import static java.util.concurrent.StructuredTaskScope.open;

public class ExceptionBehaviorDemo {

    public void demonstrateCommonExceptions() {
        try {
            log("Testing null joiner...");
            try (var scope = open(null)) {                    // ①
                log("This should not be reached");
            }
        } catch (NullPointerException e) {
            log("NullPointerException caught for null joiner");
        }

        try {
            Callable<? extends String> nullCallable = null;   // ②
            try (var scope = open(StructuredTaskScope.Joiner.
                    <String>allSuccessfulOrThrow())) {
                scope.fork(nullCallable);                       // ③
            }
        } catch (NullPointerException e) {
            log("NullPointerException caught for null callable");
        }

        try {
            var scope = open(StructuredTaskScope.Joiner.
                    <String>allSuccessfulOrThrow());
            scope.close();                                    // ④

            scope.fork(() -> "This should fail");             // ⑤

        } catch (IllegalStateException e) {
            log("IllegalStateException caught for closed scope operation");
        }
    }

    void main() {
        var demo = new ExceptionBehaviorDemo();
        demo.demonstrateCommonExceptions();
    }
}
