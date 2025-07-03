package ca.bazlur.modern.concurrency.c06;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.http.HttpRequest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MultithreadedHttpServer {
    private static final int PORT = 8080;
    private static final AtomicInteger requestCounter = new AtomicInteger(0);
    private static final int CONNECTION_THREADS = 10;

    public static void main(String[] args) throws IOException {
        System.out.println("Multi-threaded HTTP Server starting on port " + PORT);
        System.out.println("Features: Concurrent connections, Request pipelining");
        System.out.println("Connection pool size: " + CONNECTION_THREADS);

        try (ServerSocket serverSocket = new ServerSocket(PORT);
             ExecutorService connectionExecutor =
                     Executors.newFixedThreadPool(CONNECTION_THREADS)) {  //①

            serverSocket.setReuseAddress(true);

            while (true) {
                Socket clientSocket = serverSocket.accept();  //②
                System.out.println("New connection from: " +
                        clientSocket.getRemoteSocketAddress());

                // Handle each connection in a separate thread
                connectionExecutor.submit(() -> handleConnection(clientSocket)); //③
            }
        }
    }

    private static void handleConnection(Socket socket) {
        String clientAddr = socket.getRemoteSocketAddress().toString();
        System.out.println("Thread " + Thread.currentThread().getName() +
                " handling connection from: " + clientAddr);

        // Same request processing logic as BlockingHttpServer
        // but running in a separate thread ④
        try (socket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(
                     socket.getOutputStream(), true)) {

            socket.setSoTimeout(5000);
            boolean keepAlive = true;

            while (keepAlive) {
                HttpRequest request = parseRequest(in);
                if (request == null) break;

                int requestId = requestCounter.incrementAndGet();
                System.out.println("Thread " + Thread.currentThread().getName() +
                        " - Request #" + requestId + ": " +
                        request.method + " " + request.path);

                keepAlive = "keep-alive".equalsIgnoreCase(
                        request.getHeader("Connection"));

                processRequest(request);
                sendResponse(out, request, requestId, keepAlive);
            }
        } catch (Exception e) {
            System.err.println("Connection error from " + clientAddr +
                    ": " + e.getMessage());
        }
    }
}
