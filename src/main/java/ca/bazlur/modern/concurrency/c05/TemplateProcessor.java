package ca.bazlur.modern.concurrency.c05;

public class TemplateProcessor {
  private static final ScopedValue<Integer> RECURSION_DEPTH
      = ScopedValue.newInstance(); // ①
  private static final int MAX_NESTING_LEVEL = 50;

  public String processTemplate(String template) {
    if (!RECURSION_DEPTH.isBound()) {                                                  // ②
      return ScopedValue.where(RECURSION_DEPTH, 0)                                   // ③
          .call(() -> processTemplateInternal(template));
    } else {
      return processTemplateInternal(template);
    }
  }

  private String processTemplateInternal(String template) {
    int currentDepth = RECURSION_DEPTH.get();        // ④

    if (currentDepth >= MAX_NESTING_LEVEL) {         // ⑤
      throw new TemplateProcessingException(
          "Template nesting too deep: " + currentDepth + " levels");
    }

    StringBuilder result = new StringBuilder();

    // Simplified template processing logic
    int includeStart = template.indexOf("{{include:");
    if (includeStart >= 0) {
      int includeEnd = template.indexOf("}}", includeStart);
      String includePath = template.substring(includeStart + 10, includeEnd);

      String nestedContent = ScopedValue
          .where(RECURSION_DEPTH, currentDepth + 1)    // ⑥
          .call(() -> processTemplateInternal(loadTemplate(includePath)));

      result.append(template, 0, includeStart);
      result.append(nestedContent);
      // Recursively process remaining part to handle multiple includes at same level
      result.append(processTemplateInternal(template.substring(includeEnd + 2)));
    } else {
      result.append(template);
    }

    return result.toString();         // ⑦
  }

  private String loadTemplate(String path) {
    return switch (path) {
      case "header.tpl" -> "<!-- HEADER START -->";
      case "footer.tpl" -> "<!-- FOOTER END -->";
      default -> {
        if (path.startsWith("level")) {
          int level = Integer.parseInt(path.replaceAll("[^0-9]", ""));
          if (level < 10) {
            yield "Level " + level
                + " {{include:level" + (level + 1) + ".tpl}}";
          }
        }
        yield "<!-- Template content from " + path + " -->";
      }
    };
  }

  static class TemplateProcessingException extends RuntimeException {
    public TemplateProcessingException(String message) {
      super(message);
    }
  }

  public static void main(String[] args) {
    TemplateProcessor processor = new TemplateProcessor();

    // Example 1: Simple template without nesting
    String simpleTemplate = "Hello, this is a simple template!";
    System.out.println("Simple template result:");
    System.out.println(processor.processTemplate(simpleTemplate));
    System.out.println();

    // Example 2: Template with nested includes
    String nestedTemplate = "Header: {{include:header.tpl}} " +
        "Content goes here {{include:footer.tpl}}";
    System.out.println("Nested template result:");
    System.out.println(processor.processTemplate(nestedTemplate));
    System.out.println();

    // Example 3: Demonstrate recursion depth tracking
    String deeplyNested = "Level 0 {{include:level1.tpl}}";
    System.out.println("Processing deeply nested template...");
    try {
      String result = processor.processTemplate(deeplyNested);
      System.out.println("Result: " + result);
    } catch (TemplateProcessingException e) {
      System.err.println("Error: " + e.getMessage());
    }
  }
}
