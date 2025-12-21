package ca.bazlur.modern.concurrency.c06;

import module java.base;
import ca.bazlur.modern.concurrency.c06.enumeration.AlertType;
import ca.bazlur.modern.concurrency.c06.model.PriceAlert;
import ca.bazlur.modern.concurrency.c06.model.PriceData;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// todo: missing some methods
public class CryptoPriceMonitor {
  private static final List<String> EXCHANGES =
      List.of("Binance", "Coinbase", "Kraken");
  private static final List<String> SYMBOLS =
      List.of("BTC/USD", "ETH/USD", "SOL/USD");

  // Sinks for broadcasting alerts to multiple subscribers  // ①
  private static final Sinks.Many<PriceAlert> alertSink =
      Sinks.many().multicast().onBackpressureBuffer();

  public static void main(String[] args) throws InterruptedException {
    // Create merged stream from multiple exchanges  // ②
    Flux<PriceData> priceStream = Flux.merge(
        EXCHANGES.stream()
            .map(CryptoPriceMonitor::createExchangeFeed)
            .toList()
    );

    // Group prices by symbol for parallel processing  // ③
    priceStream
        .groupBy(PriceData::symbol)
        .subscribe(symbolFlux -> {
          String symbol = symbolFlux.key();

          // Calculate 5-second moving average  // ④
          symbolFlux
              .window(Duration.ofSeconds(5))
              .flatMap(window -> calculateMovingAverage(window, symbol))
              .subscribe(avg -> System.out.printf(
                  "📊 %s Moving Avg: $%.2f%n", symbol, avg));

          // Detect rapid price changes  // ⑤
          symbolFlux
            .buffer(2, 1)
            .filter(buffer -> buffer.size() == 2)
            .map(buffer -> detectRapidChange(buffer.get(0), buffer.get(1)))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .subscribe(alertSink::tryEmitNext);
        });

    // Subscribe to alerts  // ⑥
    alertSink.asFlux()
        .subscribe(alert -> System.out.printf("🚨 [%s] %s: %s%n",
            alert.type(), alert.symbol(), alert.message()));

    Thread.sleep(30000);
  }

  private static Flux<PriceData> createExchangeFeed(String exchange) {
    return Flux.interval(Duration.ofMillis(100 + (int) (Math.random() * 400)))  // ⑦
        .map(i -> {
          String symbol = SYMBOLS.get((int) (Math.random() * SYMBOLS.size()));
          double basePrice = getBasePrice(symbol);
          double variation = (Math.random() - 0.5) * 0.01;
          double price = basePrice * (1 + variation);

          return new PriceData(exchange, symbol, price, Instant.now());
        })
        .doOnNext(price -> System.out.printf("💹 %s [%s]: $%.2f%n",  // ⑧
            price.exchange(), price.symbol(), price.price()));
  }

  private static double getBasePrice(String symbol) {
    return switch (symbol) {
      case "BTC/USD" -> 50_000d;
      case "ETH/USD" -> 3_000d;
      case "SOL/USD" -> 150d;
      default -> 1_000d;
    };
  }

  private static Flux<Double> calculateMovingAverage(Flux<PriceData> window,
                                                     String symbol) {
    return window
        .collectList()
        .flux()
        .map(prices -> prices.stream()
            .mapToDouble(PriceData::price)
            .average()
            .orElse(getBasePrice(symbol)));
  }

  private static Optional<PriceAlert> detectRapidChange(PriceData prev,
                                                        PriceData current) {
    double changePercent = (current.price() - prev.price()) / prev.price();
    if (Math.abs(changePercent) >= 0.02) {
      String direction = changePercent > 0 ? "surged" : "plunged";
      String message = String.format("%s by %.2f%% to $%.2f",
          direction, changePercent * 100, current.price());
      return Optional.of(new PriceAlert(current.symbol(), message,
          AlertType.RAPID_CHANGE));
    }
    return Optional.empty();
  }

}
