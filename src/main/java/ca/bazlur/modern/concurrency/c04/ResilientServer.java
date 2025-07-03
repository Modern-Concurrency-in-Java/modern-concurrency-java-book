package ca.bazlur.modern.concurrency.c04;

import module java.base;
import java.io.IOException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.atomic.AtomicInteger;

import static ca.bazlur.modern.concurrency.c04.Utils.log;
import static java.util.concurrent.StructuredTaskScope.open;

public class ResilientServer {

    private final AtomicInteger connectionCount = new AtomicInteger(0);
    private final AtomicInteger activeConnections = new AtomicInteger(0);

    // todo: replace `main` with actual code from the book
    void main() {
        try {
            ServerSocket socket = new ServerSocket(8080);
            serve(socket);
        } catch (IOException e) {
            log("Socket error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log("Thread interrupted. Stopping.");
        }
    }

    public void serve(ServerSocket serverSocket)
            throws IOException, InterruptedException {
        log("Server starting on port: " +
                serverSocket.getLocalPort());

        try (var scope = open(StructuredTaskScope.Joiner.<Void>awaitAll())) {

            serverSocket.setSoTimeout(1000);

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket socket = serverSocket.accept();
                    int connId = connectionCount.incrementAndGet();
                    activeConnections.incrementAndGet();

                    log("Accepted connection #" + connId);

                    // Fork a task to handle this connection
                    scope.fork(() -> {
                        handleConnection(socket, connId);
                        return null;
                    });

                } catch (SocketTimeoutException e) {
                    continue; // Check for interruption
                }
            }

            log("Server stopping, waiting for " +
                    "connections to finish...");
            scope.join(); // Wait for all connections to complete

        } finally {
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
            log("Server shutdown complete. Total connections: " +
                    connectionCount.get());
        }
    }

    private void handleConnection(Socket socket, int connectionId) {
        try (socket;
                var reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                var writer = new PrintWriter(
                        socket.getOutputStream(), true)) {

            log("  [Conn-" + connectionId + "] Started");
            writer.println("Welcome to Echo Server! " +
                    "Type 'quit' to exit.");

            String line;
            while ((line = reader.readLine()) != null) {
                log("  [Conn-" + connectionId + "] Received: " + line);

                if ("quit".equalsIgnoreCase(line.trim())) {
                    writer.println("Goodbye!");
                    break;
                }

                // Echo back the message
                writer.println("Echo: " + line);
            }

            log("  [Conn-" + connectionId + "] Completed successfully");

        } catch (IOException e) {
            log("  [Conn-" + connectionId + "] Error: " +
                    e.getMessage());
        } finally {
            activeConnections.decrementAndGet();
            log("  [Conn-" + connectionId + "] Finished. Active: " +
                    activeConnections.get());
        }
    }

}
