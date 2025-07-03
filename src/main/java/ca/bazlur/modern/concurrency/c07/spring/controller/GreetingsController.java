package ca.bazlur.modern.concurrency.c07.spring.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// todo: should we include a spring application for these demo?
@RestController
public class GreetingsController {
    private static final Logger LOGGER
            = LoggerFactory.getLogger(GreetingsController.class.getName());

    @GetMapping("/hello")
    public String hello() {
        LOGGER.info("Received request for /hello");
        LOGGER.info("Running on {}", Thread.currentThread());

        return "Hello from Spring Boot";
    }
}
