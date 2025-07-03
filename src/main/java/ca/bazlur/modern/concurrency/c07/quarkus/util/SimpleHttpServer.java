package ca.bazlur.modern.concurrency.c07.quarkus.util;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.Executors;

public class SimpleHttpServer {

    private static final String[] GREETINGS = {
            "Hello, world!",
            "Hi there!",
            "Greetings!",
            "Good day!",
            "Hey!",
            "Howdy!",
            "Hola!",
            "Bonjour!",
            "Ciao!"
    };

    public static void main(String[] args) throws IOException {
        int port = 8081;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/remote/greetings", new GreetingHandler());
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        System.out.println("Server started on port " + port);
        server.start();
    }

    static class GreetingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String response = getRandomGreeting();
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }

        private String getRandomGreeting() {
            Random random = new Random();
            return GREETINGS[random.nextInt(GREETINGS.length)];
        }
    }
}
