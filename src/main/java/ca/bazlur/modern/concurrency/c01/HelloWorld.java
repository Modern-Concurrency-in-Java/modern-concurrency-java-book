package ca.bazlur.modern.concurrency.c01;

public class HelloWorld {

    public static void main(String[] args) {
        System.out.println("Hello, World!");
        // Displaying the thread that's executing the main method
        System.out.println("Executed by thread: " + Thread.currentThread().getName());
    }
}
