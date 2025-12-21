package ca.bazlur.modern.concurrency.c05;

public class ScopedValueDefaultsExample {
  private static final ScopedValue<String> USER_NAME = ScopedValue.newInstance();

  public static void main(String[] args) {
    // Using orElse for default values
    String userNameUnbound = USER_NAME.orElse("Guest"); //①
    System.out.println("No binding -> user name defaults to: "
        + userNameUnbound);

    // Using orElseThrow for validation
    try {
      USER_NAME.orElseThrow(() ->
          new IllegalStateException("No user name bound yet!")); //②
    } catch (IllegalStateException e) {
      System.out.println("Caught exception: " + e.getMessage());
    }

    // Within a bound scope
    ScopedValue.where(USER_NAME, "Bazlur").run(() -> {
      String boundUserName = USER_NAME.orElse("Guest"); //③
      System.out.println("Within binding -> user name is: " + boundUserName);

      // This won't throw since the value is bound
      String validatedName = USER_NAME.orElseThrow(()
          -> new IllegalStateException("No user name bound yet!")); //④
      System.out.println("Validated name: " + validatedName);
    });
  }
}
