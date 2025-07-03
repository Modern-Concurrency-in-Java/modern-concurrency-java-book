package ca.bazlur.modern.concurrency.c06;

import java.util.function.Consumer;

public class AiService {
    public String chat(String message) {
        return "Echo: " + message.toUpperCase();
    }

    public void chat(String message, Consumer<String> consumer) {
        Thread.startVirtualThread(() -> {
            try {
                String response = "Echo: " + message.toUpperCase();
                consumer.accept(response);
            } catch (Exception e) {
                consumer.accept("Error during chat: " + e.getMessage());
            }
        });
    }

    // todo: added the following
    public static void main(String[] args) throws InterruptedException {
        AiService aiService = new AiService();

        String result = aiService.chat("What is the meaning of life?");
        System.out.println(result);

        aiService.chat("Hello, how are you?", response -> {
            System.out.println("Response 1: " + response);
        });

        aiService.chat("What is your name?", response -> {
            System.out.println("Response 2: " + response);
        });

        aiService.chat("What is the meaning of life?", response -> {
            aiService.chat(response, response2 -> {
                aiService.chat(response2, response3 -> {
                    aiService.chat(response3, System.out::println);
                });
            });
        });

        Thread.sleep(100); // wait for all the threads to finish
    }
}
