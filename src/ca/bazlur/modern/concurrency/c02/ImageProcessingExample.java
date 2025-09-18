package ca.bazlur.modern.concurrency.c02;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

public class ImageProcessingExample {

  public static void main(String[] args)
      throws InterruptedException, ExecutionException {
    try (var service = Executors.newVirtualThreadPerTaskExecutor()) {// ①

      List<Callable<BufferedImage>> tasks = List.of(
          () -> resize("https://example.com/img1.jpg", 200, 200),
          () -> grayscale("https://example.com/img2.jpg"),
          () -> rotate("https://example.com/img3.jpg", 90)
      ); // ②

      List<Future<BufferedImage>> results = service.invokeAll(tasks); // ③

      // Process and save transformed images
      int i = 1;
      for (Future<BufferedImage> future : results) { // ④
        BufferedImage image = future.get(); // ⑤
        ImageIO.write(image, "jpg",
            new File("output_image" + i + ".jpg"));
        i++;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static BufferedImage resize(String url, int width, int height) {
    //Logic to download and resize the image goes here
    return null;
  }

  static BufferedImage grayscale(String url) {
    // Logic to download and convert the image to
    // grayscale goes here
    return null;
  }

  static BufferedImage rotate(String url, double angle) {
    //Logic to download and rotate the image goes here
    return null;
  }
}
