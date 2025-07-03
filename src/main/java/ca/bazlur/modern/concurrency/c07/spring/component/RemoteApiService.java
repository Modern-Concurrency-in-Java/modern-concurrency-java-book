package ca.bazlur.modern.concurrency.c07.spring.component;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class RemoteApiService {

    @Async
    public CompletableFuture<String> fetchDataFromRemoteApi() {
        try {
            Thread.sleep(1000); // Simulating an I/O-bound operation
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return CompletableFuture.completedFuture("Data fetched using virtual thread");
    }
}
