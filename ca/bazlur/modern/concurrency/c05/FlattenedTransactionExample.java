package ca.bazlur.modern.concurrency.c05;

import ca.bazlur.modern.concurrency.c05.model.Transaction;

public class FlattenedTransactionExample {
    private static final ScopedValue<Transaction> CURRENT_TRANSACTION =
            ScopedValue.newInstance();

    public static void main(String[] args) {
        performBusinessOperation();
    }

    private static void performBusinessOperation() {
        // Start outer transaction
        Transaction outerTx = new Transaction("OUTER_TX");
        ScopedValue.where(CURRENT_TRANSACTION, outerTx).run(() -> { //①
            System.out.println("Starting: " + outerTx.name());

            // Nested operation that might start its own transaction
            performNestedOperation();

            System.out.println("Committing: " + outerTx.name());
        });
    }

    private static void performNestedOperation() {
        if (CURRENT_TRANSACTION.isBound()) { //②
            // Join existing transaction
            Transaction currentTx = CURRENT_TRANSACTION.get();
            System.out.println("  Joining existing transaction: " + currentTx.name());
            performDatabaseOperation();
        } else {
            // Start new transaction if none exists
            Transaction newTx = new Transaction("NESTED_TX");
            ScopedValue.where(CURRENT_TRANSACTION, newTx).run(() -> {// ③
                System.out.println("  Starting new transaction: " + newTx.name());
                performDatabaseOperation();
            });
        }
    }

    private static void performDatabaseOperation() {
        Transaction tx = CURRENT_TRANSACTION.get();
        System.out.println("    Executing in transaction: " + tx.name()); //④
    }
}
