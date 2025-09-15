package ca.bazlur.modern.concurrency.c02;

public class VirtualThreadPriority {
  public static void main(String[] args) throws InterruptedException {
    var vt = Thread.ofVirtual().start(() -> {
      System.out.println("hello world");
      System.out.println(Thread.currentThread().getPriority());
    });


    System.out.println("vt = " + vt.getPriority());
    vt.join();
    vt.getAllStackTraces().forEach((thread, stackTraceElements) -> {
      System.out.println(thread);
    });
    for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
      System.out.println(element);

    }
  }
}
