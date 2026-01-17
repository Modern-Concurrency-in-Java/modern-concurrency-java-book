package ca.bazlur.modern.concurrency.c06;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class NonBlockingHttpServer {

  private static final int PORT = 8080;
  private static final AtomicInteger requestCounter = new AtomicInteger(0);
  private final ConcurrentLinkedQueue<PendingUpdate> pendingUpdates
          = new ConcurrentLinkedQueue<>();

  public static void main(String[] args) {
    System.out.println("Starting non-blocking NIO server...");
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
      System.out.println("Non-blocking HTTP server on port " + PORT);

      // Single-threaded event loop //②
      while (true) {
        // Process pending updates from async threads
        processPendingUpdates(); //③

        selector.select(100); //④
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = selectedKeys.iterator();

        while (iterator.hasNext()) {
          SelectionKey key = iterator.next();
          iterator.remove();

          try {
            if (key.isAcceptable()) {
              handleAccept(key, selector); //⑤
            } else if (key.isReadable()) {
              handleRead(key); //⑥
            } else if (key.isWritable()) {
              handleWrite(key); //⑦
            }
          } catch (IOException e) {
            System.err.println("Error handling key: "
                    + e.getMessage());
            key.cancel();
            if (key.channel() != null) {
              key.channel().close();
            }
          }
        }

        // Process any pending requests
        processAllPendingRequests(selector);
      }
    }
  }

  private void processPendingUpdates() {
    PendingUpdate update;
    while ((update = pendingUpdates.poll()) != null) {
      try {
        if (update.key.isValid()) {
          update.key.interestOps(update.key.interestOps()
                  | SelectionKey.OP_WRITE);
        }
      } catch (Exception e) {
        System.err.println("Error updating key: " + e.getMessage());
      }
    }
  }

  static class PendingUpdate {
    final SelectionKey key;

    PendingUpdate(SelectionKey key) {
      this.key = key;
    }
  }

  private void handleAccept(SelectionKey key, Selector selector)
          throws IOException {
    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
    SocketChannel clientChannel = serverChannel.accept();  //①
    if (clientChannel != null) {
      clientChannel.configureBlocking(false);
      clientChannel.register(selector, SelectionKey.OP_READ,
              new ClientState()); //②
      System.out.println("Accepted connection from: " +
              clientChannel.getRemoteAddress());
    }
  }

  static class ClientState {
    ByteBuffer readBuffer = ByteBuffer.allocate(8192);
    StringBuilder requestBuilder = new StringBuilder();
    ConcurrentLinkedQueue<String> responseQueue =
            new ConcurrentLinkedQueue<>(); //③
    Queue<HttpRequest> pendingRequests = new LinkedList<>();
    boolean keepAlive = true;
    boolean isProcessing = false; //④
  }

  private void handleRead(SelectionKey key) throws IOException {
    SocketChannel channel = (SocketChannel) key.channel();
    ClientState state = (ClientState) key.attachment();
    int bytesRead = channel.read(state.readBuffer);  //①
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
      processCompleteRequests(state, key); //②
    }
  }

  private void processCompleteRequests(ClientState state, SelectionKey key) {
    String buffer = state.requestBuilder.toString();

    while (true) {
      HttpRequest request = parseHttpRequest(buffer); //③
      if (request == null) {
        break; // No complete request found
      }
      state.pendingRequests.offer(request);
      buffer = request.remainingBuffer;

      // Check keep-alive based on HTTP version and headers
      String connection = request.getHeader("Connection");
      if (request.version.equals("HTTP/1.0")) {
        // HTTP/1.0 defaults to close unless keep-alive is explicit
        state.keepAlive = "keep-alive".equalsIgnoreCase(connection);
      } else {
        // HTTP/1.1 defaults to keep-alive unless close is explicit
        state.keepAlive = !"close".equalsIgnoreCase(connection);
      }
    }
    state.requestBuilder = new StringBuilder(buffer);

    if (!state.responseQueue.isEmpty()) {
      key.interestOps(key.interestOps() | SelectionKey.OP_WRITE); //④
    }
  }

  private HttpRequest parseHttpRequest(String buffer) {
    int requestEndIndex = buffer.indexOf("\r\n\r\n");
    if (requestEndIndex == -1) {
      requestEndIndex = buffer.indexOf("\n\n");
      if (requestEndIndex == -1) {
        return null; // No complete request yet
      }
    }
    String requestText = buffer.substring(0, requestEndIndex);
    String[] lines = requestText.split("\r\n|\n");
    if (lines.length == 0) return null;

    // Parse request line
    String[] requestLineParts = lines[0].split(" ");
    if (requestLineParts.length != 3) return null;

    HttpRequest request = new HttpRequest();
    request.method = requestLineParts[0];
    request.path = requestLineParts[1];
    request.version = requestLineParts[2];

    for (int i = 1; i < lines.length; i++) {
      String line = lines[i];
      int colonPos = line.indexOf(':');
      if (colonPos > 0) {
        String name = line.substring(0, colonPos).trim();
        String value = line.substring(colonPos + 1).trim();
        request.headers.put(name.toLowerCase(), value);
      }
    }

    // Store remaining buffer for next request
    request.remainingBuffer = buffer.substring(
            requestEndIndex
                    + (buffer.substring(requestEndIndex)
                    .startsWith("\r\n\r\n") ? 4 : 2));
    return request;
  }

  private void handleWrite(SelectionKey key) throws IOException {
    SocketChannel channel = (SocketChannel) key.channel();
    ClientState state = (ClientState) key.attachment();

    while (!state.responseQueue.isEmpty()) {
      String response = state.responseQueue.peek();
      ByteBuffer buffer
              = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));
      int written = channel.write(buffer);
      if (buffer.hasRemaining()) {
        // Socket buffer is full, try again later
        break;
      }
      // Response fully written, remove from queue
      state.responseQueue.poll();
    }

    if (state.responseQueue.isEmpty()) {
      // No more data to write, stop watching for write events
      key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
      if (!state.keepAlive && state.pendingRequests.isEmpty()) {
        // Close connection if not keep-alive and no pending requests
        System.out.println("Closing connection: "
                + channel.getRemoteAddress());
        channel.close();
        key.cancel();
      }
    }
  }

  private void processAllPendingRequests(Selector selector) {
    for (SelectionKey key : selector.keys()) {
      if (!key.isValid() || key.attachment() == null)
        continue;

      ClientState state = (ClientState) key.attachment();
      if (state.isProcessing || state.pendingRequests.isEmpty())
        continue;

      state.isProcessing = true;  //①

      while (!state.pendingRequests.isEmpty()) {
        HttpRequest request = state.pendingRequests.poll();
        int requestId = requestCounter.incrementAndGet();
        System.out.println("Request #" + requestId + ": "
                + request.method + " " + request.path);

        // Simulate async processing
        CompletableFuture.runAsync(() -> { //②
          String response;
          try {
            // Process request (simulate the work)
            if (request.path.equals("/slow")) {
              Thread.sleep(2000); // Simulate slow operation
              System.out.println(" Slow request processed");
            } else {
              System.out.println(" Fast request processed");
            }
            response = buildHttpResponse(request, requestId,
                    state.keepAlive);
          } catch (Exception e) {
            System.err.println("Error processing request #" + requestId
                    + ": " + e.getMessage());
            response = buildErrorResponse(request, requestId, e);
          }
          state.responseQueue.offer(response); //③

          // Queue the update for the selector thread
          pendingUpdates.offer(new PendingUpdate(key)); //④
          selector.wakeup();
        });
      }
      state.isProcessing = false;
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
        // todo: missing this method
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

  private String buildHttpResponse(HttpRequest request,
                                   int requestId, boolean keepAlive) {
    StringBuilder response = new StringBuilder();

    // Match the request’s HTTP version
    response.append(request.version).append(" 200 OK\r\n");

    // Headers
    response.append("Content-Type: text/plain\r\n");
    response.append("Server: NonBlockingHttpServer/1.0\r\n");
    response.append("Date: ").append(Instant.now()).append("\r\n");

    // Handle connection header properly
    if (request.version.equals("HTTP/1.0") && keepAlive) {
      response.append("Connection: keep-alive\r\n");
      return null;
    } else if (request.version.equals("HTTP/1.1") && !keepAlive) {
      response.append("Connection: close\r\n");
    }

    // For HTTP/1.1 with keep-alive, no Connection header needed (it’s defaulted)
    String body = String.format(
            "Request #%d processed\nPath: %s\nMethod: %s\nTime: %s\nThread: %s\n",
            requestId,
            request.path,
            request.method,
            Instant.now(),
            Thread.currentThread().getName());
    response.append("Content-Length: ").append(body.length()).append("\r\n");
    response.append("\r\n"); // Empty line between headers and body
    response.append(body);
    return response.toString();
  }

  private String buildErrorResponse(HttpRequest request,
                                    int requestId, Exception error) {
    StringBuilder response = new StringBuilder();

    // Match the request’s HTTP version
    response.append(request.version).append(" 500 Internal Server Error\r\n");

    // Headers
    response.append("Content-Type: text/plain\r\n");
    response.append("Server: NonBlockingHttpServer/1.0\r\n");
    response.append("Date: ").append(Instant.now()).append("\r\n");

    // Always close connection on error
    response.append("Connection: close\r\n");

    // Body
    String body = String.format(
            "Request #%d failed\nPath: %s\nError: %s\nTime: %s",
            requestId,
            request.path,
            error.getMessage() != null
                    ? error.getMessage()
                    : error.getClass().getSimpleName(),
            Instant.now());
    response.append("Content-Length: ")
            .append(body.length()).append("\r\n")
            .append("\r\n") // Empty line between headers and body
            .append(body);
    return response.toString();
  }
}

class HttpRequest {
  String method;
  String path;
  String version;
  Map<String, String> headers = new HashMap<>();
  String remainingBuffer; // For non-blocking server

  String getHeader(String name) {
    return headers.get(name.toLowerCase());
  }
}
