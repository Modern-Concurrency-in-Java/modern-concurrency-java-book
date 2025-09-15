package ca.bazlur.modern.concurrency.c04.model;

public record Review(Long id, String comment, int rating, Long productId) {
}
