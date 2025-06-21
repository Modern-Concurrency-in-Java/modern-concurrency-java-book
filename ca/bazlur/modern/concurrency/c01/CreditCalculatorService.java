package ca.bazlur.modern.concurrency.c01;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import static java.util.concurrent.CompletableFuture.*;

public class CreditCalculatorService {

    public Credit calculateCredit(Long personId) {
        var person = getPerson(personId); // Database call - blocks thread
        var assets = getAssets(person); // API call - blocks thread
        var liabilities = getLiabilities(person); // Database call - blocks thread
        importantWork(); // CPU work

        return calculateCredits(assets, liabilities);
    }

    public Credit calculateCreditWithUnboundedThreads(Long personId) throws InterruptedException {
        var person = getPerson(personId);

        var assetsRef = new AtomicReference<List<Asset>>(); // ①
        var t1 = new Thread(() -> {
            var assets = getAssets(person);
            assetsRef.set(assets); // ②
        });

        var liabilitiesRef = new AtomicReference<List<Liability>>();
        Thread t2 = new Thread(() -> {
            var liabilities = getLiabilities(person);
            liabilitiesRef.set(liabilities);
        });

        var t3 = new Thread(() -> importantWork()); // ③

        t1.start(); // ④
        t2.start();
        t3.start();

        t1.join(); // ⑤
        t2.join();

        var credit = calculateCredits(assetsRef.get(), liabilitiesRef.get()); // ⑥

        t3.join(); // ⑦

        return credit;
    }

    public Credit calculateCreditWithExecutor(Long personId) throws ExecutionException, InterruptedException {
        try (ExecutorService executor = Executors.newFixedThreadPool(5)) { // ①
            var person = getPerson(personId);

            var assetsFuture = executor.submit(() -> getAssets(person)); // ②
            var liabilitiesFuture = executor.submit(() -> getLiabilities(person)); // ③
            executor.submit(() -> importantWork()); // ④

            return calculateCredits(assetsFuture.get(), liabilitiesFuture.get()); // ⑤
        }
    }

    // TODO: show usage in ParallelExecutorDemo
    public Credit calculateCreditWithCompletableFuture(Long personId) throws InterruptedException, ExecutionException {
        return runAsync(() -> importantWork()) // ①
                .thenCompose(_ -> supplyAsync(() -> getPerson(personId))) // ②
                .thenCombineAsync(supplyAsync(() -> getAssets(getPerson(personId))), // ③
                        (person, assets) -> calculateCredits(assets, getLiabilities(person))) // ④
                .get(); // ⑤
    }

    public Credit calculateCreditWithVirtualThread(Long personId) throws ExecutionException, InterruptedException {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) { // ①
            var person = getPerson(personId);
            var assetsFuture = executor.submit(() -> getAssets(person));
            var liabilitiesFuture = executor.submit(() -> getLiabilities(person));
            executor.submit(this::importantWork);

            return calculateCredits(assetsFuture.get(), liabilitiesFuture.get());
        }
    }

    // Simulated methods with 200ms delay each
    private Person getPerson(Long personId) {
        simulateDelay(200);
        return new Person(personId, "John Doe");
    }

    private List<Asset> getAssets(Person person) {
        simulateDelay(200);
        return List.of(
                new Asset("House", 300000),
                new Asset("Car", 25000));
    }

    private List<Liability> getLiabilities(Person person) {
        simulateDelay(200);
        return List.of(
                new Liability("Mortgage", 200000),
                new Liability("Credit Card", 5000));
    }

    private void importantWork() {
        simulateDelay(200);
        System.out.println("Important work completed");
    }

    private Credit calculateCredits(List<Asset> assets, List<Liability> liabilities) {
        simulateDelay(200);
        double totalAssets = assets.stream().mapToDouble(Asset::value).sum();
        double totalLiabilities = liabilities.stream().mapToDouble(Liability::amount).sum();
        double creditScore = (totalAssets - totalLiabilities) / 1000;
        return new Credit(creditScore);
    }

    private void simulateDelay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

}

// Credit calculation models
record Credit(double score) {
}

record Person(Long id, String name) {
}

record Asset(String type, double value) {
}

record Liability(String type, double amount) {
}