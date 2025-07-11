package ca.bazlur.modern.concurrency.c05;

// todo: merged all ScopedValue examples here
public class ScopedValueExamples {

    public static void main(String[] args) throws InterruptedException {
        ScopedValue<String> NAME = ScopedValue.newInstance();

        Runnable task = () -> {
            if (NAME.isBound()) {
                System.out.println("Name is bound: " + NAME.get());
            } else {
                System.out.println("Name is not bound");
            }
        };

        task.run();  // unbounded

        // Execute within scope
        ScopedValue.where(NAME, "Bazlur").run(task);  // bounded

        // Try to execute outside scope
        task.run();  // unbounded

        Thread thread = Thread.ofPlatform().unstarted(task);
        ScopedValue.where(NAME, "Bazlur").run(thread::start);  // unbounded
        thread.join();

        Thread anotherThread = Thread.ofVirtual().start(() -> {
            ScopedValue.where(NAME, "Bazlur").run(task);  // bounded
        });
        anotherThread.join();
    }
}
