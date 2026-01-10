package ca.bazlur.modern.concurrency.c04;

import module java.base;
import com.sun.management.HotSpotDiagnosticMXBean;

import java.lang.management.ManagementFactory;

import static java.util.concurrent.StructuredTaskScope.open;

public class DocumentProcessor {

  private final ThreadFactory threadFactory = Thread.ofVirtual()
      .name("doc-proc", 1)
      .factory();

  public DocumentReport processDocument(String documentId)
      throws InterruptedException {
    try (var gatheringScope = open(
          StructuredTaskScope.Joiner.<String>allSuccessfulOrThrow(),
             conf -> conf.withThreadFactory(threadFactory)
                          .withName("doc-gathering-scope"))) { // ①

      var headerTask = gatheringScope.fork(() ->
          fetchHeader(documentId));     // ②
      var bodyTask = gatheringScope.fork(() ->
          fetchBody(documentId));       // ③
      var metadataTask = gatheringScope.fork(() ->
          fetchMetadata(documentId));   // ④

      gatheringScope.join();                                          // ⑤

      return analyzeContent(headerTask.get(),
          bodyTask.get(), metadataTask.get());
    } catch (StructuredTaskScope.FailedException e) {
      throw new RuntimeException("Failed to gather document content", e);
    }
  }

//  public DocumentReport processDocument(String documentId)
//          throws InterruptedException {
//    try (var gatheringScope = open(
//            StructuredTaskScope.Joiner.<String>allSuccessfulOrThrow(),
//            conf -> conf.withThreadFactory(threadFactory)
//                    .withName("doc-gathering-scope"))) { // ①
//      var headerTask = gatheringScope.fork(() ->
//              fetchHeader(documentId)); // ②
//      var bodyTask = gatheringScope.fork(() ->
//              fetchBody(documentId)); // ②
//      var metadataTask = gatheringScope.fork(() ->
//              fetchMetadata(documentId)); // ②
//
//      gatheringScope.join(); // ③
//
//      return analyzeContent(
//              headerTask.get(), bodyTask.get(), metadataTask.get());
//    } catch (StructuredTaskScope.FailedException e) {
//      throw new RuntimeException("Failed to gather document content", e);
//    }
//  }

  private DocumentReport analyzeContent(String header,
                                        String body,
                                        String metadata)
      throws InterruptedException {

    try (var analysisScope = open(StructuredTaskScope.Joiner.
        allSuccessfulOrThrow())) {                          // ①

      var wordCountTask = analysisScope.fork(() -> countWords(body));                        // ②
      var sentimentTask = analysisScope.fork(() -> analyzeSentiment(body));                  // ③
      var summaryTask = analysisScope.fork(() -> generateSummary(header, body, metadata));   // ④

      analysisScope.join();              // ⑤

      return new DocumentReport(
          wordCountTask.get(),
          sentimentTask.get(),
          summaryTask.get()
      );
    } catch (StructuredTaskScope.FailedException e) {
      throw new RuntimeException("Failed to analyze document content", e);
    }
  }

  private String fetchHeader(String documentId) throws InterruptedException {
    Thread.sleep(Duration.ofSeconds(10));        // ⑥
    return "Header for document " + documentId;
  }

  private String fetchBody(String documentId) throws InterruptedException {
    Thread.sleep(Duration.ofSeconds(10));
    return "This is the main content of document " + documentId;
  }

  private String fetchMetadata(String documentId) throws InterruptedException {
    Thread.sleep(Duration.ofSeconds(10));
    return "Created: 2024-01-01, Author: John Doe";
  }

  private Integer countWords(String content) throws InterruptedException {
    Thread.sleep(Duration.ofMillis(100));
    return content.split("\\s+").length;
  }

  private String analyzeSentiment(String content) throws InterruptedException {
    Thread.sleep(Duration.ofMillis(200));
    return content.toLowerCase().contains("important") ? "Positive" : "Neutral";
  }

  private String generateSummary(String header, String body, String metadata)
      throws InterruptedException {
    Thread.sleep(Duration.ofMillis(150));
    return header + ": " + body.substring(0, Math.min(50, body.length())) + "...";
  }

  void main() {
    var processor = new DocumentProcessor();
    try {
      var report = processor.processDocument("DOC-123");
      System.out.println(report);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      System.out.println("Processing interrupted");
    }
  }

  public record DocumentReport(int wordCount,
                               String sentiment, String summary) {
    @Override
    public String toString() {
      return String.format("Document Report:\n  Words: %d\n  " +
              "Sentiment: %s\n  Summary: %s",
          wordCount, sentiment, summary);
    }
  }
}
