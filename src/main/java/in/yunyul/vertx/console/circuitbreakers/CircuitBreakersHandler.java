package in.yunyul.vertx.console.circuitbreakers;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

public class CircuitBreakersHandler {
    private static final String JSON_CONTENT_TYPE = "application/json";

    public CircuitBreakersHandler(Vertx vertx, Router router, String basePath, String circuitBreakerAddress) {
        // Set up streaming
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        // Allow circuit breaker broadcasts
        PermittedOptions breakerPermitted = new PermittedOptions().setAddress(circuitBreakerAddress);
        BridgeOptions options = new BridgeOptions()
                // No incoming messages permitted
                .addOutboundPermitted(breakerPermitted);
        sockJSHandler.bridge(options);
        router.route(basePath + "/circuitbreakerproxy/*").handler(sockJSHandler);
        router.route(basePath + "/circuitbreakers/address").produces(JSON_CONTENT_TYPE).handler(ctx ->
                ctx.response().putHeader("content-type", JSON_CONTENT_TYPE)
                        .end(new JsonObject().put("address", circuitBreakerAddress).toString()));
    }
}
