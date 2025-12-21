package ca.bazlur.modern.concurrency.c04;

import module java.base;

import static java.util.concurrent.StructuredTaskScope.*;

public class OrderProcessingService {

    private static OrderResult handleOrderProcessingError(Throwable cause) {
        return switch (cause) { // ③
            case PaymentDeclinedException pde -> new OrderResult(null,
                    "PAYMENT_FAILED",
                    """
                            Your payment was declined. Please check your
                            card details or try a different payment method.""",
                    false);
            case InsufficientInventoryException iie -> new OrderResult(null, "OUT_OF_STOCK",
                    """
                            Sorry, this item is currently out of stock.
                            We'll notify you when it becomes available.""",
                    false);
            case ShippingNotAvailableException snae -> new OrderResult(null, "SHIPPING_UNAVAILABLE",
                    """
                            We can't ship to your address right now.
                            Please contact customer service for
                            alternatives.""", false);
            case NetworkException ne -> new OrderResult(null,
                    "TEMPORARY_ERROR",
                    """
                            We're experiencing technical difficulties.
                            Please try again in a few minutes.""", false);
            case SecurityException se -> new OrderResult(null,
                    "SECURITY_CHECK_FAILED",
                    """
                            Additional verification required.
                            Please contact customer service.""", false);
            default -> // ④
                    new OrderResult(null, "SYSTEM_ERROR",
                            """
                                    Something went wrong on our end. \
                                    Please try again or contact support.""", false);
        };
    }

    void main() {
        var demo = new OrderProcessingService();
        var result1 = demo.processOrder("user123", "outofstock", 3500);
        System.out.println("Out of stock scenario: " + result1);

        var result2 = demo.processOrder("declined_user", "laptop", 1200);
        System.out.println("Payment declined scenario: " + result2);

        var result3 = demo.processOrder("remote_customer", "book", 25);
        System.out.println("Shipping unavailable scenario: " + result3);

        var result4 = demo.processOrder("user456", "laptop", 1200);
        System.out.println("Successful order: " + result4);
    }

    public OrderResult processOrder(String customerId,
                                    String productId, double amount) {

        try (var scope = open(Joiner.<String>allSuccessfulOrThrow())) { // ①

            var paymentTask = scope.fork(() ->
                    processPayment(customerId, amount));
            var inventoryTask = scope.fork(() ->
                    checkAndReserveInventory(productId));
            var shippingTask = scope.fork(() ->
                    calculateShipping(customerId, productId));

            var results = scope.join()
                    .map(Subtask::get)
                    .toList();

            String orderId = generateOrderId();
            return new OrderResult(orderId, "CONFIRMED",
                    "Order confirmed successfully", true);

        } catch (FailedException e) {
            Throwable cause = e.getCause(); // ②
            return handleOrderProcessingError(cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operation interrupted", e);
        }
    }

    private String processPayment(String customerId, double amount)
            throws PaymentDeclinedException, NetworkException,
            SecurityException, InterruptedException {
        Thread.sleep(Duration.ofMillis(200));

        // Simulate different payment scenarios
        if (amount > 5000.0) {
            throw new SecurityException("High-value transaction requires " +
                    "additional verification");
        }
        if (customerId.contains("declined")) {
            throw new PaymentDeclinedException("Insufficient funds");
        }
        if (customerId.contains("network")) {
            throw new NetworkException("Payment gateway timeout");
        }

        return "Payment processed: $" + amount;
    }

    private String checkAndReserveInventory(String productId)
            throws InsufficientInventoryException, NetworkException,
            InterruptedException {
        Thread.sleep(Duration.ofMillis(150));

        if (productId.contains("outofstock")) {
            throw new InsufficientInventoryException(
                    "Only 0 items available, requested 1");
        }
        if (productId.contains("network")) {
            throw new NetworkException("Inventory service unavailable");
        }

        return "Reserved inventory for " + productId;
    }

    private String calculateShipping(String customerId, String productId)
            throws ShippingNotAvailableException, NetworkException,
            InterruptedException {
        Thread.sleep(Duration.ofMillis(100));

        if (customerId.contains("remote")) {
            throw new ShippingNotAvailableException(
                    "No shipping available to remote location");
        }
        if (productId.contains("hazardous")) {
            throw new ShippingNotAvailableException(
                    "Cannot ship hazardous materials to this address");
        }
        if (customerId.contains("network")) {
            throw new NetworkException("Shipping service unavailable");
        }

        return "Shipping calculated: $12.99";
    }

    private String generateOrderId() {
        return "ORD-" + System.currentTimeMillis();
    }

    public record OrderResult(String orderId,
                              String status,
                              String message,
                              boolean successful) {
    }

    public static class PaymentDeclinedException extends Exception {
        public PaymentDeclinedException(String message) {
            super(message);
        }
    }

    public static class InsufficientInventoryException extends Exception {
        public InsufficientInventoryException(String message) {
            super(message);
        }
    }

    public static class ShippingNotAvailableException extends Exception {
        public ShippingNotAvailableException(String message) {
            super(message);
        }
    }

    public static class NetworkException extends Exception {
        public NetworkException(String message) {
            super(message);
        }
    }
}
