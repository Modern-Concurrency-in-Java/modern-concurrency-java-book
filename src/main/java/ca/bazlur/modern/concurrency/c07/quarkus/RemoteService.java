package ca.bazlur.modern.concurrency.c07.quarkus;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

// todo: restructure quarkus examples
@Path("/remote")
@RegisterRestClient
public interface RemoteService {
    @Path("greetings")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String greetings();
}
