package ca.bazlur.modern.concurrency.c02;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PhraseGeneratorExample {

    public static void main(String[] args) {
        PhraseGeneratorExample phraseGeneratorExample = new PhraseGeneratorExample();
        try {
            System.out.println(phraseGeneratorExample.generatePhrase());
        } catch (ExecutionException | InterruptedException e) {
        }
    }

    public String generatePhrase() throws ExecutionException, InterruptedException {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<String> adjectiveFuture = executor.submit(this::fetchAdjective);
            Future<String> nounFuture = executor.submit(this::fetchNoun);

            String adjective = adjectiveFuture.get();
            String noun = nounFuture.get();

            return adjective + " " + noun;
        }
    }

    private String fetchAdjective() {
        // Fetch adjective from an API
        return "beautiful";
    }

    private String fetchNoun() {
        // Fetch noun from an API
        return "sunset";
    }
}
