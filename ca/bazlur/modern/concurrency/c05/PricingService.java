package ca.bazlur.modern.concurrency.c05;

public class PricingService {
    private static final ScopedValue<Double> DISCOUNT_RATE = ScopedValue.newInstance();

    public double calculatePrice(double basePrice) {
        // Using call() to return the calculated price from within the scope
        return ScopedValue.where(DISCOUNT_RATE, 0.20)  // 20% discount
                .call(() -> basePrice * (1 - DISCOUNT_RATE.get()));
    }

    void main() {
        PricingService service = new PricingService();
        double finalPrice = service.calculatePrice(100.0);
        System.out.println("Final price: $" + finalPrice);  // Output: Final price: $80.0
    }
}
