package ca.bazlur.modern.concurrency.c02;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ImageProcessingExample {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        try (var service = Executors.newVirtualThreadPerTaskExecutor()) {// ①

            List<Callable<BufferedImage>> tasks = new ArrayList<>(); // ②
            tasks.add(() -> resizeImage("https://example.com/image1.jpg", 200, 200));
            tasks.add(() -> applyGrayscale("https://example.com/image2.jpg"));
            tasks.add(() -> rotateImage("https://example.com/image3.jpg", 90));

            List<Future<BufferedImage>> results = service.invokeAll(tasks); // ③

            // Process and save transformed images
            int i = 1;
            for (Future<BufferedImage> future : results) { // ④
                BufferedImage image = future.get(); // ⑤
                ImageIO.write(image, "jpg", new File("output_image" + i + ".jpg"));
                i++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Placeholder image transformation functions
    static BufferedImage resizeImage(String url, int width, int height) throws Exception {
        // ... Logic to download and resize the image ...
    }

    static BufferedImage applyGrayscale(String url) throws Exception {
        // ... Logic to download and convert the image to grayscale ...
    }

    static BufferedImage rotateImage(String url, double angle) throws Exception {
        // ... Logic to download and rotate the image ...
    }
}
