package ca.bazlur.modern.concurrency.c02;

import java.util.concurrent.StructuredTaskScope;

public class StructuredConcurrencyExample {

    public static void main(String[] args) {
        try (var scope = StructuredTaskScope.open()) {
            StructuredTaskScope.Subtask<String> subtask1 = scope
                    .fork(() -> fetchData("https://dummyjson.com/users/1?select=lastName"));
            StructuredTaskScope.Subtask<String> subtask2 = scope
                    .fork(() -> fetchData("https://dummyjson.com/users/1?select=firstName"));

            scope.join();

            var result = subtask2.get() + " " + subtask1.get();
            System.out.println(result);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static String fetchData(String url) {
        // implement your own logic
        return "John";
    }
}
