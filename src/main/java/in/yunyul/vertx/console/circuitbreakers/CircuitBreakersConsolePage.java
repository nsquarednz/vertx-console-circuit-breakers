package in.yunyul.vertx.console.circuitbreakers;

import in.yunyul.vertx.console.base.ConsolePage;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

@SuppressWarnings("unused")
public class CircuitBreakersConsolePage implements ConsolePage {
    private final String circuitBreakerAddress;

    /**
     * Creates the circuit breakers console page
     *
     * @return the console page
     */
    public static CircuitBreakersConsolePage create() {
        return new CircuitBreakersConsolePage("vertx.circuit-breaker");
    }

    /**
     * Creates the circuit breakers console page, listening on the specified address
     * the event bus address to listen for changes on
     *
     * @param circuitBreakerAddress
     * @return
     */
    public static CircuitBreakersConsolePage create(String circuitBreakerAddress) {
        return new CircuitBreakersConsolePage(circuitBreakerAddress);
    }

    private CircuitBreakersConsolePage(String circuitBreakerAddress) {
        this.circuitBreakerAddress = circuitBreakerAddress;
    }

    @Override
    public void mount(Vertx vertx, Router router, String basePath) {
        new CircuitBreakersHandler(vertx, router, basePath, circuitBreakerAddress);
    }

    @Override
    public String getLoaderFileName() {
        return "/js/circuitbreakers.js";
    }
}
