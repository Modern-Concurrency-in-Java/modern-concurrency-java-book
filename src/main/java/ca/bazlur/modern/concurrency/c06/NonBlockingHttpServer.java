package ca.bazlur.modern.concurrency.c06;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static ca.bazlur.modern.concurrency.c06.HttpRequestParser.parseHttpRequest;

public class NonBlockingHttpServer {
    private static final int PORT = 8080;
    private static final AtomicInteger requestCounter = new AtomicInteger(0);

    public static void main(String[] args) {
        System.out.println("Non-blocking HTTP Server with NIO Selector starting...");
        System.out.println("Features: Single-threaded event loop, " +
                "Non-blocking I/O, High concurrency");

        try {
            new NonBlockingHttpServer().start();
        } catch (IOException e) {
            System.err.println("Server failed to start: " + e.getMessage());
        }
    }

    private void start() throws IOException {
        try (Selector selector = Selector.open();
             ServerSocketChannel serverChannel = ServerSocketChannel.open()) {

            serverChannel.bind(new InetSocketAddress(PORT));
            serverChannel.configureBlocking(false);  //①
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Non-blocking HTTP Server started on port " + PORT);

            // Single-threaded event loop  //②
            while (true) {
                selector.select();  //③

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    try {
                        if (key.isAcceptable()) {
                            handleAccept(key, selector);  //④
                        } else if (key.isReadable()) {
                            handleRead(key);  //⑤
                        } else if (key.isWritable()) {
                            handleWrite(key);  //⑥
                        }
                    } catch (IOException e) {
                        System.err.println("Error handling key: " + e.getMessage());
                        key.cancel();
                        if (key.channel() != null) {
                            key.channel().close();
                        }
                    }
                }
            }
        }
    }

    private void handleAccept(SelectionKey key, Selector selector)
            throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept(); //⑦

        if (clientChannel != null) {
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ,
                    new ClientState()); //⑧

            System.out.println("Accepted connection from: " +
                    clientChannel.getRemoteAddress());
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ClientState state = (ClientState) key.attachment();

        int bytesRead = channel.read(state.readBuffer);  //⑩
        if (bytesRead == -1) {
            // Client disconnected
            channel.close();
            return;
        }

        if (bytesRead > 0) {
            state.readBuffer.flip();
            byte[] data = new byte[state.readBuffer.remaining()];
            state.readBuffer.get(data);
            state.requestBuilder.append(new String(data, StandardCharsets.UTF_8));
            state.readBuffer.clear();

            // Process complete requests
            processCompleteRequests(state, key);  //⑪
        }
    }

    private void processCompleteRequests(ClientState state, SelectionKey key) {
        String buffer = state.requestBuilder.toString();
        int requestEnd;

        while ((requestEnd = buffer.indexOf("\r\n\r\n")) != -1) {
            String requestData = buffer.substring(0, requestEnd);
            buffer = buffer.substring(requestEnd + 4);

            // Parse and process request asynchronously  //⑫
            HttpRequest request = parseHttpRequest(requestData);
            if (request != null) {
                int requestId = requestCounter.incrementAndGet();
                System.out.println("Request #" + requestId + ": " +
                        request.method + " " + request.path);

                // Process request without blocking
                processRequestAsync(request, state, requestId);

                // Update connection state
                state.keepAlive = "keep-alive".equalsIgnoreCase(
                        request.getHeader("Connection"));
            }
        }

        state.requestBuilder = new StringBuilder(buffer);

        // Enable writing if we have responses to send
        if (!state.responseQueue.isEmpty()) {
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);  //⑬
        }
    }

    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ClientState state = (ClientState) key.attachment();

        while (!state.responseQueue.isEmpty()) {
            String response = state.responseQueue.peek();
            ByteBuffer buffer = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));

            int written = channel.write(buffer);
            if (buffer.hasRemaining()) {
                // Socket buffer is full, try again later,
                break;
            }

            // Response fully written, remove from queue
            state.responseQueue.poll();
        }

        if (state.responseQueue.isEmpty()) {
            // No more data to write, stop watching for write events
            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);

            if (!state.keepAlive) {
                // Close connection if not keep-alive
                System.out.println("Closing connection: " + channel.getRemoteAddress());
                channel.close();
                key.cancel();
            }
        }
    }

    private void processRequestAsync(HttpRequest request, ClientState state,
                                     int requestId) {
        // Simulate async processing
        if (request.path.equals("/slow")) {
            // Instead of blocking, we schedule async work ⑭
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(2000); // Simulate slow operation
                    System.out.println("  Slow request processed");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).thenRun(() -> {
                // Add response to queue when ready ⑮
                String response = buildHttpResponse(request, requestId, state.keepAlive);
                synchronized (state.responseQueue) {
                    state.responseQueue.offer(response);
                }
            });
        } else {
            // Fast requests complete immediately
            System.out.println("  Fast request processed");
            String response = buildHttpResponse(request, requestId, state.keepAlive);
            state.responseQueue.offer(response);
        }
    }

    static class ClientState {
        ByteBuffer readBuffer = ByteBuffer.allocate(8192);
        StringBuilder requestBuilder = new StringBuilder();
        Queue<String> responseQueue = new LinkedList<>(); //⑨
        boolean keepAlive = true;
    }
}
