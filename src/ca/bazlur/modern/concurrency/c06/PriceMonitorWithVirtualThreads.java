package ca.bazlur.modern.concurrency.c06;

import ca.bazlur.modern.concurrency.c06.enumeration.AlertType;
import ca.bazlur.modern.concurrency.c06.model.PriceAlert;
import ca.bazlur.modern.concurrency.c06.model.PriceData;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class PriceMonitorWithVirtualThreads {

  private static final List<String> SYMBOLS =
      List.of("BTC/USD", "ETH/USD", "SOL/USD");
  private static final List<String> EXCHANGES =
      List.of("Binance", "Coinbase", "Kraken");

  private static final BlockingQueue<PriceAlert> alertQueue =
      new LinkedBlockingQueue<>();
  private static final Map<String, LinkedList<Double>> priceWindows =
      new ConcurrentHashMap<>();
  private static final Map<String, AtomicReference<PriceData>> latestPrices =
      new ConcurrentHashMap<>();

  private static void generatePriceFeed(String exchange) {
    var random = ThreadLocalRandom.current();

    while (!Thread.currentThread().isInterrupted()) {
      try {
        Thread.sleep(100 + random.nextInt(400)); //②

        String symbol = SYMBOLS.get(random.nextInt(SYMBOLS.size()));
        double basePrice = getBasePrice(symbol);
        double variation = (random.nextDouble() - 0.5) * 0.01;
        double price = basePrice * (1 + variation);

        PriceData priceData = new PriceData(exchange, symbol, price, Instant.now());
        System.out.printf("💹 %s [%s]: $%.2f%n", exchange, symbol, price);

        // Process this price in a new virtual thread
        Thread.startVirtualThread(() -> processPrice(priceData)); //③

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }

  private static void processPrice(PriceData currentPrice) {
    PriceData previousPrice = latestPrices
        .computeIfAbsent(currentPrice.symbol(),
            k -> new AtomicReference<>())
        .getAndSet(currentPrice); //④

    if (previousPrice != null) {
      detectRapidChange(previousPrice, currentPrice);
    }

    calculateSimpleMovingAverage(currentPrice.symbol(), currentPrice);
  }

  private static void detectRapidChange(PriceData prev, PriceData current) {
    if (!prev.symbol().equals(current.symbol())) {
      return;
    }

    double changePercent = Math.abs((current.price() - prev.price())
        / prev.price()) * 100;
    if (changePercent > 0.5) { // 0.5% change threshold
      PriceAlert alert = new PriceAlert(
          current.symbol(),
          String.format("Rapid %.2f%% change: $%.2f → $%.2f",
              changePercent, prev.price(), current.price()),
          AlertType.RAPID_CHANGE
      );
      alertQueue.offer(alert); //⑤
    }
  }

  private static void monitorThresholds() {
    Map<String, Double> thresholds = Map.of(
        "BTC/USD", 51000.0,
        "ETH/USD", 3100.0,
        "SOL/USD", 105.0
    );

    while (!Thread.currentThread().isInterrupted()) {
      try {
        Thread.sleep(2000); //⑥

        thresholds.forEach((symbol, threshold) -> {
          AtomicReference<PriceData> ref = latestPrices.get(symbol);
          if (ref != null) {
            PriceData latest = ref.get();
            if (latest != null && latest.price() > threshold) {
              alertQueue.offer(new PriceAlert(
                  symbol,
                  String.format("Price $%.2f exceeded threshold $%.2f",
                      latest.price(), threshold),
                  AlertType.THRESHOLD_CROSSED
              ));
            }
          }
        });
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }

  private static void calculateSimpleMovingAverage(String symbol,
                                                   PriceData price) {
    var window = priceWindows.computeIfAbsent(symbol,
        k -> new LinkedList<>());

    synchronized (window) { //⑦
      window.add(price.price());
      if (window.size() > 10) {
        window.removeFirst();
      }

      if (window.size() >= 5) {
        double avg = window.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);

        System.out.printf("📊 %s Moving Avg: $%.2f%n", symbol, avg);
      }
    }
  }

  private static void processAlerts() {
    while (!Thread.currentThread().isInterrupted()) {
      try {
        PriceAlert alert = alertQueue.poll(100, TimeUnit.MILLISECONDS); //⑧
        if (alert != null) {
          System.out.printf("🚨 [%s] %s: %s%n",
              alert.type(), alert.symbol(), alert.message());
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }

  private static double getBasePrice(String symbol) {
    return switch (symbol) {
      case "BTC/USD" -> 50000;
      case "ETH/USD" -> 3000;
      case "SOL/USD" -> 100;
      default -> 1000;
    };
  }

  public static void main(String[] args) throws InterruptedException {
    try (var executor
             = Executors.newVirtualThreadPerTaskExecutor()) { //①
      // Start price feeds from each exchange
      for (String exchange : EXCHANGES) {
        executor.submit(() -> generatePriceFeed(exchange));
      }

      // Start processors
      executor.submit(PriceMonitorWithVirtualThreads::processAlerts);
      executor.submit(PriceMonitorWithVirtualThreads::monitorThresholds);

      Thread.sleep(30000);
    }
  }
}
