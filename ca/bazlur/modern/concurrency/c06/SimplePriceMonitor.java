package ca.bazlur.modern.concurrency.c06;

import ca.bazlur.modern.concurrency.c06.model.PriceData;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;

public class SimplePriceMonitor {

    public static void main(String[] args) throws InterruptedException {
        // Create a stream of price updates ①
        Flux<PriceData> priceStream = Flux.interval(Duration.ofSeconds(1))
                .map(i -> new PriceData(
                        "Binance",
                        "BTC/USD",
                        50000 + (Math.random() - 0.5) * 1000, //②
                        Instant.now()
                ));

        // Process the stream ③
        priceStream
                .filter(price -> price.price() > 50200) //④
                .map(price -> String.format("Alert: BTC price $%.2f exceeds threshold!",
                        price.price())) //⑤
                .subscribe(
                        alert -> System.out.println(alert), //⑥
                        error -> System.err.println("Error: " + error),
                        () -> System.out.println("Monitoring complete")
                );

        // Keep the main thread alive
        Thread.sleep(10000);
    }
}
