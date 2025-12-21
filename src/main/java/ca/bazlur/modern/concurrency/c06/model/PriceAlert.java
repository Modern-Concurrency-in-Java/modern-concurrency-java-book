package ca.bazlur.modern.concurrency.c06.model;

import ca.bazlur.modern.concurrency.c06.enumeration.AlertType;

public record PriceAlert(String symbol, String message, AlertType type) {
}
