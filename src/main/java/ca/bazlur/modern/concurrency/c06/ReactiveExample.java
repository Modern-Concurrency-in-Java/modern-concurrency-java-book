package ca.bazlur.modern.concurrency.c06;

import reactor.core.publisher.Flux;

public class ReactiveExample {

    public static void main(String[] args) {
        // Create a Flux emitting integers 1 to 5
        Flux<Integer> numbers = Flux.just(1, 2, 3, 4, 5); // ①

        // Process the stream: filter even numbers and convert to strings
        numbers
                .filter(n -> n % 2 == 0)    // ② Keep even numbers
                .map(n -> "Value: " + n)      // ③ Transform to strings
                .subscribe(
                        System.out::println,      // ④ onNext: print each value
                        error -> System.err.println("Error: " + error),  // ⑤ onError
                        () -> System.out.println("Done!")  //⑥ onComplete
                );
    }
}
