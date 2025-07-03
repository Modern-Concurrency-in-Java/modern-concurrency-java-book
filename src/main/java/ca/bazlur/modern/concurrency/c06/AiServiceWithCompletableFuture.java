package ca.bazlur.modern.concurrency.c06;

import java.util.concurrent.CompletableFuture;

// todo: created this to demonstrate CompletableFuture. Should we replace the previous one?
public class AiServiceWithCompletableFuture {

    public CompletableFuture<String> chat(String message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return "Echo: " + message.toUpperCase();
            } catch (Exception e) {
                return "Error during chat: " + e.getMessage();
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        AiServiceWithCompletableFuture aiService = new AiServiceWithCompletableFuture();

        aiService.chat("What is the meaning of life?")
                .thenCompose(aiService::chat)
                .thenCompose(aiService::chat)
                .thenCompose(aiService::chat)
                .thenAccept(System.out::println);
    }
}
