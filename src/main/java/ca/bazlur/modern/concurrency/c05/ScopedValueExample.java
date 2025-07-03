package ca.bazlur.modern.concurrency.c05;

// todo: merged all ScopedValue examples here
public class ScopedValueExample {
    public static void main(String[] args) throws InterruptedException {

        ScopedValue<String> NAME = ScopedValue.newInstance();

        Runnable task = () -> {
            if (NAME.isBound()) {
                System.out.println("Name is bound: " + NAME.get());
            } else {
                System.out.println("Name is not bound");
            }
        };

        // todo: added the following comments
        task.run();  // unbounded

        ScopedValue.where(NAME, "Bazlur").run(task);  // bounded

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
