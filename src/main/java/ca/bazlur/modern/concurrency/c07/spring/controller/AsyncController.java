package ca.bazlur.modern.concurrency.c07.spring.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;

@RestController
public class AsyncController {
	private static final Logger LOGGER
			= LoggerFactory.getLogger(AsyncController.class.getName());

	@GetMapping("/async-call")
	public Callable<String> handleAsyncRequest() {
		return () -> {
			Thread.sleep(500); // Simulate an I/O-bound operation
			LOGGER.info("Running on {}", Thread.currentThread());
			return "Hello from Virtual Thread!";
		};
	}
}
