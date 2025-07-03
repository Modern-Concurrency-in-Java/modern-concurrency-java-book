package ca.bazlur.modern.concurrency.c06;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.http.HttpRequest;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockingHttpServer {
    private static final int PORT = 8080;
    private static final AtomicInteger requestCounter = new AtomicInteger(0);

    public static void main(String[] args) throws IOException {
        System.out.println("Blocking HTTP Server starting on port " + PORT);
        System.out.println("Features: Single-threaded, Request pipelining");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            serverSocket.setReuseAddress(true); //①

            while (true) {
                Socket clientSocket = serverSocket.accept(); //②
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

            socket.setSoTimeout(5000); //③
            boolean keepAlive = true;

            while (keepAlive) {
                HttpRequest request = parseRequest(in); //④
                if (request == null) {
                    break; // Connection closed
                }

                int requestId = requestCounter.incrementAndGet();
                System.out.println("Request #" + requestId + ": " +
                        request.method + " " + request.path);

                // Check for keep-alive
                keepAlive = "keep-alive".equalsIgnoreCase(
                        request.getHeader("Connection"));

                // Process request - this may take time!  //⑤
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

    static void processRequest(HttpRequest request) {
        // Simulate different processing times
        if (request.path.equals("/slow")) {
            try {
                Thread.sleep(2000); // Simulate slow operation ⑥
                System.out.println("  Slow request processed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            System.out.println("  Fast request processed");
        }
    }
}
