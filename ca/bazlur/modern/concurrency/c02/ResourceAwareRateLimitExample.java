package ca.bazlur.modern.concurrency.c02;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class ResourceAwareRateLimitExample {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)) // ①
            .build();

    private static final int MAX_PARALLEL = 10; // ②
    private static final Semaphore gate = new Semaphore(MAX_PARALLEL); // ③
    private static final String API_URL = "https://api.chucknorris.io/jokes/random";

    public static void main(String[] args) throws Exception {
        Instant start = Instant.now();

        List<String> jokes = fetchJokes(50); // ④

        long ms = Duration.between(start, Instant.now()).toMillis();
        System.out.printf("Fetched %d jokes in %d ms (avg %d ms)%n",
                jokes.size(), ms, ms / jokes.size());

        jokes.stream().limit(3).forEach(j -> System.out.println("• " + j));
    }

    private static List<String> fetchJokes(int n) throws Exception {
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) { // ⑤
            List<Future<String>> futures = IntStream.range(0, n)
                    .mapToObj(_ -> pool.submit(ResourceAwareRateLimitExample::fetchJoke))
                    .toList();

            return futures.stream()
                    .map(ResourceAwareRateLimitExample::join) // ⑥
                    .toList();
        }
    }

    private static String fetchJoke() throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(API_URL))
                .GET()
                .timeout(Duration.ofSeconds(30)) // ⑦
                .build();

        try {
            gate.acquire(); // ⑧
            HttpResponse<String> res = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                throw new RuntimeException("API error " + res.statusCode());
            }

            return parseJoke(res.body());
        } finally {
            gate.release(); // ⑨
        }
    }

    private static String parseJoke(String json) { // ⑩
        int s = json.indexOf("\"value\":\"") + 9;
        int e = json.indexOf('"', s);
        return json.substring(s, e).replace("\\\"", "\"");
    }

    private static <T> T join(Future<T> f) {
        try {
            return f.get();
        } catch (InterruptedException e) { // ⑪
            Thread.currentThread().interrupt();
            throw new CompletionException(e);
        } catch (ExecutionException e) {
            throw new CompletionException(e.getCause());
        }
    }
}
