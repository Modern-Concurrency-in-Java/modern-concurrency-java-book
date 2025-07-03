package ca.bazlur.modern.concurrency.c06;

import java.time.Instant;

public record PriceData(String exchange, String symbol, double price,
                        Instant timestamp) {
}
