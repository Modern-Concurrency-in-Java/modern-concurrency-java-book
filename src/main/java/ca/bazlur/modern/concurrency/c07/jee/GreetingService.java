package ca.bazlur.modern.concurrency.c07.jee;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Random;

@ApplicationScoped
public class GreetingService {
    private final List<String> greetings = List.of(
        "Hello, World!",
        "Greetings from Virtual Threads!",
        "Welcome to Jakarta EE!",
        "Virtual threads are amazing!"
    );
    
    private final Random random = new Random();
    
    public String getRandomGreeting() {
        return greetings.get(random.nextInt(greetings.size()));
    }
}