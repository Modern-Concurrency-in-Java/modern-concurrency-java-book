package ca.bazlur.modern.concurrency.c06;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockingHttpServer {
    private static final int PORT = 8080;
    private static final AtomicInteger requestCounter = new AtomicInteger(0);

    public static void main(String[] args) throws IOException {
        System.out.println("Blocking HTTP Server starting on port " + PORT);
        System.out.println("Features: Single-threaded, Request pipelining");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            serverSocket.setReuseAddress(true);  // ①

            while (true) {
                Socket clientSocket = serverSocket.accept();  // ②
                handleConnection(clientSocket);
            }
        }
    }

    private static void handleConnection(Socket socket) {
        System.out.println("New connection from: " +
                socket.getRemoteSocketAddress());

        try (socket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(
                     socket.getOutputStream(), true)) {

            socket.setSoTimeout(5000);  // ③
            boolean keepAlive = true;

            while (keepAlive) {
                HttpRequest request = parseRequest(in);  // ④
                if (request == null) {
                    break; // Connection closed
                }

                int requestId = requestCounter.incrementAndGet();
                System.out.println("Request #" + requestId + ": " +
                        request.method + " " + request.path);

                // Check for keep-alive
                keepAlive = "keep-alive".equalsIgnoreCase(
                        request.getHeader("Connection"));

                // Process request - this may take time!  // ⑤
                processRequest(request);

                // Send response
                sendResponse(out, request, requestId, keepAlive);
            }

        } catch (SocketTimeoutException e) {
            System.out.println("Connection timeout");
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    private static HttpRequest parseRequest(BufferedReader in)
            throws IOException {
        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isBlank()) {
            return null;
        }

        String[] requestParts = requestLine.split(" ");
        if (requestParts.length < 3) {
            return null;
        }

        HttpRequest request = new HttpRequest();
        request.method = requestParts[0];
        request.path = requestParts[1];
        request.version = requestParts[2];

        String line;
        while ((line = in.readLine()) != null && !line.isBlank()) {
            int idx = line.indexOf(':');
            if (idx > 0) {
                String name = line.substring(0, idx).trim().toLowerCase();
                String value = line.substring(idx + 1).trim();
                request.headers.put(name, value);
            }
        }

        return request;
    }

    private static void sendResponse(PrintWriter out,
                                     HttpRequest request,
                                     int requestId,
                                     boolean keepAlive) {
        // Send HTTP response
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/plain");
        out.println("Connection: " + (keepAlive ? "keep-alive" : "close"));

        String body = String.format(
                "Request #%d processed\nPath: %s\nTime: %s\n",
                requestId, request.path, Instant.now());

        out.println("Content-Length: " + body.length());
        out.println(); // Empty line between headers and body
        out.print(body);
        out.flush(); //⑥
    }

    static void processRequest(HttpRequest request)
            throws IOException {
        try {
            if (request.path.startsWith("/slow")) {
                Thread.sleep(Duration.of(30, ChronoUnit.SECONDS)); // ⑦
                System.out.println("  Slow request processed");
            } else if (request.path.startsWith("/medium")) {
                Thread.sleep(Duration.of(500, ChronoUnit.MILLIS));
                System.out.println("  Medium request processed");
            } else {
                Thread.sleep(Duration.of(100, ChronoUnit.MILLIS));
                System.out.println("  Fast request processed");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static class HttpRequest {
        String method;
        String path;
        String version;
        Map<String, String> headers = new HashMap<>();
        String remainingBuffer; // For non-blocking server

        String getHeader(String name) {
            return headers.get(name.toLowerCase());
        }
    }
}
