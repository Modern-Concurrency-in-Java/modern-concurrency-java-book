package ca.bazlur.modern.concurrency.c06;

import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestParser {
    public static class CustomHttpRequest {
        public final String method;
        public final String path;
        private final Map<String, String> headers;
        
        public CustomHttpRequest(String method, String path) {
            this.method = method;
            this.path = path;
            this.headers = new HashMap<>();
        }
        
        public String getHeader(String name) {
            return headers.get(name.toLowerCase());
        }
        
        public void setHeader(String name, String value) {
            headers.put(name.toLowerCase(), value);
        }
    }
    
    public static CustomHttpRequest parseHttpRequest(String requestLine) {
        String[] parts = requestLine.split(" ");
        if (parts.length >= 2) {
            return new CustomHttpRequest(parts[0], parts[1]);
        }
        return new CustomHttpRequest("GET", "/");
    }
}