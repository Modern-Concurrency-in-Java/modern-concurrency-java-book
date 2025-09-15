package ca.bazlur.modern.concurrency.c06;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.concurrent.atomic.AtomicLong;

// todo: figure out best way to add maven projects here
public class VertxHttpServer extends AbstractVerticle {
    private static final int PORT = 8080;
    private final AtomicLong requestCounter = new AtomicLong(0); //①
    private long startTime;

    @Override
    public void start(Promise<Void> startPromise) {
        startTime = System.currentTimeMillis();
        Router router = Router.router(vertx); //②

        router.get("/fast").handler(this::handleFastRequest);
        router.get("/slow").handler(this::handleSlowRequest);
        router.get("/stats").handler(this::handleStats);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(PORT, "localhost"); //③
    }

    private void handleFastRequest(RoutingContext ctx) {
        long requestId = requestCounter.incrementAndGet();

        ctx.response()
                .putHeader("content-type", "text/plain")
                .end("Request #" + requestId + ": Fast request processed"); //④
    }

    private void handleSlowRequest(RoutingContext ctx) {
        long requestId = requestCounter.incrementAndGet();

        vertx.setTimer(2000, id -> { //⑤
            ctx.response()
                    .putHeader("content-type", "text/plain")
                    .end("Request #" + requestId + ": Slow request processed");
        });
    }

    private void handleStats(RoutingContext ctx) {
        long uptimeMillis = System.currentTimeMillis() - startTime;

        JsonObject stats = new JsonObject()
                .put("totalRequests", requestCounter.get())
                .put("uptimeMillis", uptimeMillis)
                .put("currentThread", Thread.currentThread().getName())
                .put("isEventLoopThread", Vertx.currentContext()
                                    .isEventLoopContext()); //⑥

        ctx.response()
                .putHeader("content-type", "application/json")
                .end(stats.encodePrettily());
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new VertxHttpServer()); //⑦
    }
}
