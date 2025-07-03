package ca.bazlur.modern.concurrency.c06;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class PipeliningLoadTest {

    public static void main(String[] args) throws Exception {
        int numConnections = 10;
        int requestsPerConnection = 100;

        long startTime = System.currentTimeMillis();

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numConnections; i++) {
            Thread t = Thread.ofVirtual().start(() -> {  //①
                try {
                    testPipelining(requestsPerConnection);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            threads.add(t);
        }

        for (Thread t : threads) {
            t.join();
        }

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("Total time: " + elapsed + "ms");
        System.out.println("Requests per second: " +
                (numConnections * requestsPerConnection * 1000.0 / elapsed));
    }

    private static void testPipelining(int numRequests)
            throws Exception {
        Socket socket = new Socket("localhost", 8080);
        PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

        for (int i = 0; i < numRequests; i++) {
            String path = (i % 10 == 0) ? "/slow" : "/fast";  //②
            out.println("GET " + path + " HTTP/1.1");
            out.println("Host: localhost");
            out.println("Connection: " +
                    (i == numRequests - 1 ? "close" : "keep-alive"));  //③
            out.println();
        }

        for (int i = 0; i < numRequests; i++) {
            readResponse(in);  //④
        }

        socket.close();
    }

    static void readResponse(BufferedReader in)
            throws IOException {
        // Read status line and headers
        String line;
        int contentLength = 0;

        while ((line = in.readLine()) != null) {
            System.out.println(line);
            if (line.startsWith("Content-Length: ")) {
                contentLength = Integer.parseInt(
                        line.substring("Content-Length: ".length()));
            }
            if (line.isEmpty()) {
                break; // End of headers
            }
        }

        // Read body
        char[] body = new char[contentLength];
        in.read(body);
        System.out.println(new String(body));
    }
}
