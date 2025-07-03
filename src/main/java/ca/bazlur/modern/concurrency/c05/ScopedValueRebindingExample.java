package ca.bazlur.modern.concurrency.c05;

public class ScopedValueRebindingExample {
    private static final ScopedValue<String> USER_ROLE = ScopedValue.newInstance();

    public static void main(String[] args) {
        // Bind initial value in outer scope
        ScopedValue.where(USER_ROLE, "Admin").run(() -> { //①
            System.out.println("Outer scope: User role is " + USER_ROLE.get());
            performTask();

            // Rebind in nested scope
            ScopedValue.where(USER_ROLE, "Guest").run(() -> { //②
                System.out.println("Inner scope: User role is " + USER_ROLE.get());
                performTask();
            }); //③

            // Original value restored automatically
            System.out.println("Back to outer scope: User role is " + USER_ROLE.get());
            performTask();
        });
    }

    public static void performTask() {
        System.out.println("  Performing task as: " + USER_ROLE.get()); //④
    }
}
