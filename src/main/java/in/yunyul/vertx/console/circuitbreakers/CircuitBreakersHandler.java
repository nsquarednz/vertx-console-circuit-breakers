package in.yunyul.vertx.console.circuitbreakers;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.*;

import java.util.HashMap;
import java.util.Map;

class CircuitBreakersHandler {
    private static final String JSON_CONTENT_TYPE = "application/json";
    private Map<String, JsonObject> lastReceived = new HashMap<>();

    CircuitBreakersHandler(Vertx vertx, Router router, String basePath, String circuitBreakerAddress) {
        // Set up streaming
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        // Allow circuit breaker broadcasts
        PermittedOptions breakerPermitted = new PermittedOptions().setAddress(circuitBreakerAddress);
        BridgeOptions options = new BridgeOptions()
                // No incoming messages permitted
                .addOutboundPermitted(breakerPermitted);
        sockJSHandler.bridge(options, be -> {
            be.complete(true);
            // Send last values on handler registration
            if (be.type() == BridgeEventType.REGISTER) {
                for (JsonObject value : lastReceived.values()) {
                    // Copies envelope structure from EventBusBridgeImpl##deliverMessage
                    be.socket().write(Buffer.buffer(new JsonObject()
                            .put("type", "rec")
                            .put("address", circuitBreakerAddress)
                            .put("body", value)
                            .encode()
                    ));
                }
            }
        });

        router.route(basePath + "/circuitbreakerproxy/*").handler(sockJSHandler);
        router.route(basePath + "/circuitbreakers/address").produces(JSON_CONTENT_TYPE).handler(ctx ->
                ctx.response().putHeader("content-type", JSON_CONTENT_TYPE)
                        .end(new JsonObject().put("address", circuitBreakerAddress).toString()));

        MessageConsumer<JsonObject> consumer = vertx.eventBus().consumer(circuitBreakerAddress);
        consumer.handler(msg -> lastReceived.put(msg.body().getString("name"), msg.body()));
    }
}
