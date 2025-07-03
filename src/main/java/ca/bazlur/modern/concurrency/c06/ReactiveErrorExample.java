package ca.bazlur.modern.concurrency.c06;

import reactor.core.publisher.Flux;

public class ReactiveErrorExample {

    public static void main(String[] args) {
        Flux.just(1, 2, 3, 0, 5)
                .map(number -> 10 / number)
                .subscribe(
                        System.out::println,
                        Throwable::printStackTrace,
                        () -> System.out.println("Done!")
                );
    }
}
