package ca.bazlur.modern.concurrency.c04.model;

import java.util.List;

public record ProductInfo(Product product, List<Review> reviews) {
}
