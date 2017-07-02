package in.yunyul.vertx.console.circuitbreakers;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

public class CircuitBreakersHandler {
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
    }
}
