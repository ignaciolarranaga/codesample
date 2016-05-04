package com.example.trial.weather;

import com.example.trial.weather.domain.DataPoint;
import com.example.trial.weather.impl.RestWeatherCollectorEndpoint;
import com.example.trial.weather.impl.RestWeatherQueryEndpoint;
import com.example.trial.weather.repository.Repository;
import com.example.trial.weather.repository.RepositoryFactory;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import static org.hamcrest.Matchers.is;
import org.junit.AfterClass;
import static org.junit.Assert.assertThat;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This test intends to validate several extreme conditions
 * (high load, memory, etc.). Those test are ignored by default as they are not
 * suitable for continuous execution.
 * @author ignacio
 */
public class NaivePerformanceTest {

    private static final Logger LOGGER = Logger.getLogger(NaivePerformanceTest.class.getName());

    /**
     * The base URL to be used in the tests (notice it is different from
     * production to avoid collisions).
     */
    private static final String BASE_URL = "http://localhost:9091";

    /**
     * The server used for the test.
     */
    private static HttpServer server;

    /**
     * The collect end point to be used in the test.
     */
    private static WebTarget collect;

    /**
     * The query end point to be used in the test.
     */
    private static WebTarget query;

    @BeforeClass
    public static void init() throws IOException, InterruptedException {
        Repository repository = RepositoryFactory.getInstance();
        repository.reset();

        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(RestWeatherCollectorEndpoint.class);
        resourceConfig.register(RestWeatherQueryEndpoint.class);

        server = GrizzlyHttpServerFactory.createHttpServer(
            URI.create(BASE_URL), resourceConfig, false);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdownNow();
        }));

        server.start();

        Client client = ClientBuilder.newClient();
        collect = client.target(BASE_URL + "/collect");
        query = client.target(BASE_URL + "/query");
    }

    @AfterClass
    public static void shutdown() {
        server.shutdown();
    }

    /**
     * This test validates a large number of airports to be defined and the
     * system to continue working
     */
    @Test @Ignore("Ignored as it is considered too large to run normally")
    public void testLargeNumberOfAirports() {
        checkCollectPing();

        Set<String> usedIATACodes = new HashSet<>();
        int count = 5000;
        while (usedIATACodes.size() < count) {
            String iata = RandomStringUtils.random(3, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");

            // Careful, if the number is too high the combinations will be
            // already tested and the loop will last forever.
            // Maximum is 17,576: https://www.quora.com/How-many-combinations-of-three-letters-in-a-26-letter-alphabet-are-there
            if (! usedIATACodes.contains(iata)) {
                if (addTheSampleAirport(iata) == Response.Status.OK.getStatusCode()) {
                    usedIATACodes.add(iata);
                }
            }
        }

        checkAirportsSize(count);
    }

    /**
     * This test validates a medium number of airports with a large number of
     * data points to be defined and the system to continue working.
     */
    @Test @Ignore
    public void testMediumNumberOfAirportsAndDataPoints() {
        long startTime = System.currentTimeMillis();

        checkCollectPing();

        List<String> usedIATACodes = new ArrayList<>();
        int count = 1000;
        while (usedIATACodes.size() < count) {
            String iata = RandomStringUtils.random(3, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");

            // Careful, if the number is too high the combinations will be
            // already tested and the loop will last forever.
            // Maximum is 17,576: https://www.quora.com/How-many-combinations-of-three-letters-in-a-26-letter-alphabet-are-there
            if (! usedIATACodes.contains(iata)) {
                if (addTheSampleAirport(iata) == Response.Status.OK.getStatusCode()) {
                    usedIATACodes.add(iata);
                }
            }
        }

        for (int i=0; i<10000; i++) {
            updateRandomWeatherDataPoint(usedIATACodes);
        }

        long endTime = System.currentTimeMillis();
        LOGGER.info("testMediumNumberOfAirportsAndDataPoints execution takes "
            + ((endTime - startTime) / 1000.0) + " secs. to create "
            + usedIATACodes.size() + " airports and perform 10.000 updates.");
    }

    private void updateRandomWeatherDataPoint(List<String> usedIATACodes) {
        Random random = new Random();
        final DataPoint.Type[] dataPoints = DataPoint.Type.values();
        
        // Determining the random numbers
        String iata = usedIATACodes.get(random.nextInt(usedIATACodes.size()));
        int mean = random.nextInt(99) + 1;  // Range (0,100]
        String dataPointType = "precipitation";

        WebTarget path = collect.path("/weather/" + iata + "/" + dataPointType);
        DataPoint dataPoint = new DataPoint(10, 50, 100, mean, 4);
        Response response = path.request().post(Entity.entity(dataPoint,
                MediaType.APPLICATION_JSON));
        String errorMessage = response.getStatus() !=
            Response.Status.OK.getStatusCode() ? response.readEntity(String.class) : "";
        assertThat("Checking the response status to be OK for " + iata + "/"
            + dataPointType + "/" + dataPoint + "(" + errorMessage + ")",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));

        response.close();
    }

    private void checkAirportsSize(int expected) {
        WebTarget path = collect.path("/airports");
        Response response = path.request().get();
        assertThat("Checking the response status of listing airports to be OK.",
                response.getStatus(), is(Response.Status.OK.getStatusCode()));

        assertThat("Checking the number of airports defined.",
            ((Set<String>) response.readEntity(Set.class)).size(), is(expected));

        response.close();
    }

    private void checkDataSize(int expected) throws JsonSyntaxException {
        WebTarget path = query.path("/ping");
        Response response = path.request().get();
        assertThat("Checking the response status to be OK.",
                response.getStatus(), is(Response.Status.OK.getStatusCode()));

        // Valindating the datasize
        JsonElement pingResult = new JsonParser().parse(response.readEntity(String.class));
        assertThat("Validating there are exaclty " + expected + " measurements",
                pingResult.getAsJsonObject().get("datasize").getAsInt(), is(expected));

        response.close();
    }
    
    private int addTheSampleAirport(String iata) {
        Response response = null;
        try {
            WebTarget path = collect.path("/airport/" + iata + "/42.364347/-71.005181/");
            response = path.request().post(null, Response.class);
            return response.getStatus();
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private void checkCollectPing() {
        WebTarget path = collect.path("/ping");
        Response response = path.request().get();
        assertThat("Checking the response status to be OK.",
                response.getStatus(), is(Response.Status.OK.getStatusCode()));

        assertThat("Checking the response entity to be 1.",
                response.readEntity(Integer.class), is(1));

        response.close();
    }

}
