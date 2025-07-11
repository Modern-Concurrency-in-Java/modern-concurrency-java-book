package ca.bazlur.modern.concurrency.c06;

import module java.base;
import ca.bazlur.modern.concurrency.c06.model.PriceAlert;
import ca.bazlur.modern.concurrency.c06.model.PriceData;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

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

    enum AlertType {THRESHOLD_CROSSED, RAPID_CHANGE, ANOMALY}
}
