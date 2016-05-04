package com.example.trial.weather;

import com.example.trial.weather.WeatherClient;
import com.example.trial.weather.impl.RestWeatherCollectorEndpoint;
import com.example.trial.weather.impl.RestWeatherQueryEndpoint;
import com.example.trial.weather.repository.Repository;
import com.example.trial.weather.repository.RepositoryFactory;
import java.io.IOException;
import java.net.URI;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author ignaciolarranaga@gmail.com
 */
public class WebServerClientIntegrationTest {

    /**
     * The base URL to be used in the tests (notice it is different from
     * production to avoid collisions).
     */
    private static final String BASE_URL = "http://localhost:9091/";

    /**
     * The server used for the test.
     */
    private static HttpServer server;

    @BeforeClass
    public static void init() throws IOException, InterruptedException {
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(RestWeatherCollectorEndpoint.class);
        resourceConfig.register(RestWeatherQueryEndpoint.class);

        server = GrizzlyHttpServerFactory.createHttpServer(
            URI.create(BASE_URL), resourceConfig, false);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdownNow();
        }));

        server.start();
    }

    @Before
    public void initTest() {
        Repository repository = RepositoryFactory.getInstance();
        repository.reset();
    }

    @AfterClass
    public static void shutdown() {
        server.shutdown();
    }

    /**
     * This test is just a validation that the client can execute a default script
     * The logic is proven in other places
     */
    @Test
    public void testExecution() {
        WeatherClient wc = new WeatherClient(BASE_URL);
        wc.pingCollect();
        wc.addAirport("BOS", "42.364347", "-71.005181");
        wc.addAirport("EWR", "40.6925", "-74.168667");
        wc.addAirport("JFK", "40.639751", "-73.778925");
        wc.addAirport("LGA", "40.777245", "-73.872608");
        wc.addAirport("MMU", "40.79935", "-74.4148747");
        wc.populate("wind", 0, 10, 6, 4, 20);

        wc.query("BOS");
        wc.query("JFK");
        wc.query("EWR");
        wc.query("LGA");
        wc.query("MMU");

        wc.pingQuery();
        //wc.exit();
    }

}
