package ca.bazlur.modern.concurrency.c05;

public class MultiScopedExample {
    private static final ScopedValue<String> USER_ID
            = ScopedValue.newInstance();
    private static final ScopedValue<String> SESSION_ID
            = ScopedValue.newInstance();

    public static void main(String[] args) {
        ScopedValue.where(USER_ID, "user123") //①
                .where(SESSION_ID, "session456") //②
                .run(() -> performTask()); //③
    }

    public static void performTask() {
        String userId = USER_ID.get();
        String sessionId = SESSION_ID.get();
        System.out.println("Performing task for user: " + userId +
                " in session: " + sessionId);
        logAction();
    }

    public static void logAction() {
        String userId = USER_ID.get(); //④
        String sessionId = SESSION_ID.get();
        System.out.println("Logging action for user: " + userId +
                " in session: " + sessionId);
    }
}
