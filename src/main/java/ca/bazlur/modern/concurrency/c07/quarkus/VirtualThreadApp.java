package ca.bazlur.modern.concurrency.c07.quarkus;

import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Path("/greetings")
public class VirtualThreadApp {
    private static final Logger logger
            = LoggerFactory.getLogger(VirtualThreadApp.class);

    @Inject
    @RestClient
    RemoteService remoteService;

    @GET
    @RunOnVirtualThread
    @Produces(MediaType.TEXT_PLAIN)
    public String process() {
        logger.info("Received greetings request");
        var response = remoteService.greetings();
        logger.info("Received response: {}", response);
        logger.info("Running on {}", Thread.currentThread());
        return response.toUpperCase();
    }
}
