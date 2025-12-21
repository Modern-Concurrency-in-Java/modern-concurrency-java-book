package ca.bazlur.modern.concurrency.c05;

import java.awt.Color;

public class SimpleGraphicsExample {

    // Drawing context as ScopedValues
    private static final ScopedValue<Color> DRAW_COLOR
                              = ScopedValue.newInstance();   // ①
    private static final ScopedValue<Integer> LINE_WIDTH
                              = ScopedValue.newInstance();

    // Simulated drawing methods
    static void drawLine(String from, String to) {
        Color color = DRAW_COLOR.isBound() ? DRAW_COLOR.get() : Color.BLACK;   // ②
        int width = LINE_WIDTH.isBound() ? LINE_WIDTH.get() : 1;

        System.out.printf("Drawing line from %s to %s [Color: %s, Width: %d]\n",
                from, to, color.toString(), width);
    }

    static void drawRectangle(String name) {
        Color color = DRAW_COLOR.isBound() ? DRAW_COLOR.get() : Color.BLACK;
        int width = LINE_WIDTH.isBound() ? LINE_WIDTH.get() : 1;

        System.out.printf("Drawing rectangle '%s' [Color: %s, Width: %d]\n",
                name, color.toString(), width);
    }

    // Component that draws itself and its children
    static void drawButton(String label) {
        System.out.println("\n--- Drawing Button: " + label + " ---");

        // Button uses blue color with thick border
        ScopedValue.where(DRAW_COLOR, Color.BLUE)                     // ③
                .where(LINE_WIDTH, 3)
                .run(() -> {
                    drawRectangle("button-background");

                    // Text inside button uses different color
                    ScopedValue.where(DRAW_COLOR, Color.WHITE)          // ④
                            .where(LINE_WIDTH, 1)
                            .run(() -> {
                                System.out.println("Drawing text: " + label);
                            });

                    // Border automatically uses blue again
                    drawRectangle("button-border");                // ⑤
                });
    }

    // Panel that contains multiple components
    static void drawPanel() {
        System.out.println("\n--- Drawing Panel ---");

        // Panel uses gray theme
        ScopedValue.where(DRAW_COLOR, Color.GRAY)
                .where(LINE_WIDTH, 2)
                .run(() -> {
                    drawRectangle("panel-background");

                    // Draw child components - each with their own style
                    drawButton("OK");                                   // ⑥
                    drawButton("Cancel");

                    // Back to panel's gray automatically
                    drawLine("divider-start", "divider-end");          // ⑦
                });
    }

    void main() {
        System.out.println("=== Graphics Context Example ===\n");

        // Set default drawing context
        ScopedValue.where(DRAW_COLOR, Color.BLACK)                //  ⑧
                .where(LINE_WIDTH, 1)
                .run(() -> {
                    // Draw with default black color
                    drawLine("A", "B");

                    // Draw a panel (which has its own colors)
                    drawPanel();

                    // Automatically back to black after panel
                    System.out.println("\n--- Back to main context ---");
                    drawLine("C", "D");                                //  ⑨
                });

        // Outside the scope - no context available
        System.out.println("\n--- Outside any context ---");
        drawLine("E", "F");  // Will use defaults                 // ⑩
    }
}
