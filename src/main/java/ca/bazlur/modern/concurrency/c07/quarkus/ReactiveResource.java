package ca.bazlur.modern.concurrency.c07.quarkus;

import io.smallrye.common.annotation.RunOnVirtualThread;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.time.Duration;


@Path("/reactive")
@RegisterRestClient
interface ExternalService {
    @GET
    @Path("/hello")
    Uni<String> hello();
}

@Path("/reactive")
public class ReactiveResource {

    @Inject
    HelloService helloService;

    @GET
    @Path("/hello")
    @RunOnVirtualThread
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {

        return helloService.getHello()
                .await()
                .atMost(Duration.ofSeconds(5));
    }
}

@ApplicationScoped
class HelloService {
    @Inject
    @RestClient
    ExternalService externalService;

    Uni<String> getHello() {
        return externalService.hello();
    }
}
