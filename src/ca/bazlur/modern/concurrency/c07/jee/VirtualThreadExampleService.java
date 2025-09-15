package ca.bazlur.modern.concurrency.c07.jee;

import jakarta.enterprise.concurrent.ManagedExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.concurrent.ExecutionException;

@ManagedExecutorDefinition(name = "java:module/concurrent/virtual-executor",
    qualifiers = WithVirtualThreads.class,
    virtual = true)
@Path("/virtualThreads")
public class VirtualThreadExampleService {

  @Inject
  @WithVirtualThreads
  ManagedExecutorService virtualManagedExecutor;

  @Inject
  GreetingService greetingService;

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String virtualThreads() throws InterruptedException, ExecutionException {
    return virtualManagedExecutor.submit(() -> {
      System.out.println("Received request on virtual thread: "
          + Thread.currentThread());
      return greetingService.getRandomGreeting();
    }).get();
  }
}
