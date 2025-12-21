package ca.bazlur.modern.concurrency.c07.jee;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@ApplicationScoped
public class GreetingService {
    private static final List<String> GREETINGS = List.of(
            "Hello", "Hi", "Welcome", "Howdy", "Bonjour"
    );

    public String getRandomGreeting() {
        return GREETINGS.get(ThreadLocalRandom.current().nextInt(GREETINGS.size()));
    }
}
