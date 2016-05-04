package com.example.trial.weather;

import com.example.trial.weather.AirportLoader;
import com.example.trial.weather.domain.AirportData;
import com.example.trial.weather.impl.RestWeatherCollectorEndpoint;
import com.example.trial.weather.impl.RestWeatherQueryEndpoint;
import com.example.trial.weather.repository.Repository;
import com.example.trial.weather.repository.RepositoryFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import static org.hamcrest.Matchers.is;
import org.junit.AfterClass;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author ignaciolarranaga@gmail.com
 */
public class AirportLoaderIntegrationTest {

    // Sample lines for testing
    private static String INCORRECT_IATA_FILE_LINE = "1,\"General Edward Lawrence Logan Intl\",\"Boston\",\"United States\",\"BS\",\"KBOS\",42.364347,-71.005181,19,-5,\"A\"";

    /**
     * The base URL to be used in the tests (notice it is different from
     * production to avoid collisions).
     */
    private static final String BASE_URL = "http://localhost:9091/";

    /**
     * The server used for the test.
     */
    private static HttpServer server;

    /**
     * The collect end point to be used in the test.
     */
    private static WebTarget collect;

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

        Client client = ClientBuilder.newClient();
        collect = client.target(BASE_URL + "collect");
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

    @Test
    public void testProvidedSampleCaseOneByOne() throws IOException {
        try (AirportLoader loader = new AirportLoader(BASE_URL)) {
            try (Reader reader = new InputStreamReader(
                getClass().getResourceAsStream("/airports.dat"))) {
                loader.uploadOneByOne(reader);

                validateFileContentsCorrectlyUploaded();
            }
        }
    }

    @Test
    public void testProvidedSampleCaseInBatch() throws IOException {
        try (AirportLoader loader = new AirportLoader(BASE_URL)) {
            try (Reader reader = new InputStreamReader(
                getClass().getResourceAsStream("/airports.dat"))) {
                loader.uploadInBatch(reader, 5);

                validateFileContentsCorrectlyUploaded();
            }
        }
    }

    @Test
    public void testWrongFormatedInputForSingleUpload() throws IOException {
        try (AirportLoader loader = new AirportLoader(BASE_URL)) {
            try (Reader reader = new StringReader("Unformatted !!·!DQWADAS·!\"")) {
                loader.uploadOneByOne(reader);

                validateItHasNoAirports();
            }
        }
    }

    @Test
    public void testWrongFormatedInputForBatchUpload() throws IOException {
        try (AirportLoader loader = new AirportLoader(BASE_URL)) {
            try (Reader reader = new StringReader("Unformatted !!·!DQWADAS·!\"")) {
                loader.uploadInBatch(reader, 5);
                // Validate each single airport data is correctly loaded

                WebTarget path = collect.path("/airports");
                Response response = path.request().get();
                assertThat("Checking the response status to be OK",
                    response.getStatus(), is(Response.Status.OK.getStatusCode()));

                assertThat("Validate there are no airports defined.",
                    ((Set<String>) response.readEntity(Set.class)).size(), is(0));
            }
        }
    }

    @Test
    public void testWrongAirportExecutedCorrectlyInOneByOne() throws IOException {
        try (AirportLoader loader = new AirportLoader(BASE_URL)) {
            try (Reader reader = new StringReader(INCORRECT_IATA_FILE_LINE)) {
                loader.uploadOneByOne(reader);

                validateItHasNoAirports();
            }
        }
    }

    @Test
    public void testWrongAirportExecutedCorrectlyInBatch() throws IOException {
        try (AirportLoader loader = new AirportLoader(BASE_URL)) {
            try (Reader reader = new StringReader(INCORRECT_IATA_FILE_LINE)) {
                loader.uploadInBatch(reader, 5);

                validateItHasNoAirports();
            }
        }
    }

    /**
     * This method tests that all the items provided in the same file are
     * correctly stored in the repository
     */
    private void validateFileContentsCorrectlyUploaded() {
        WebTarget path = collect.path("/airports");
        Response response = path.request().get();
        assertThat("Checking the response status to be OK",
                response.getStatus(), is(Response.Status.OK.getStatusCode()));

        Set<AirportData> expectedSet = new HashSet<>();
        expectedSet.add(new AirportData("BOS", 42.364347, -71.005181));
        expectedSet.add(new AirportData("EWR", 40.6925, -74.168667));
        expectedSet.add(new AirportData("JFK", 40.639751, -73.778925));
        expectedSet.add(new AirportData("LCY", 51.505278, 0.055278));
        expectedSet.add(new AirportData("LGA", 40.777245, -73.872608));
        expectedSet.add(new AirportData("LHR", 51.4775, -0.461389));
        expectedSet.add(new AirportData("LTN", 51.874722, -0.368333));
        expectedSet.add(new AirportData("LPL", 53.333611, -2.849722));
        expectedSet.add(new AirportData("MMU", 40.79935, -74.4148747));
        expectedSet.add(new AirportData("STN", 51.885, 0.235));
        assertThat("Validate it contains the airports from the file.",
            (Set<String>) response.readEntity(Set.class),
            is(expectedSet.stream()
                    .map(d -> d.getIata())
                    .collect(Collectors.toSet())));

        // Validate each single airport data is correctly loaded
        for (AirportData expected : expectedSet) {
            checkAirportInformationIsCorrect(expected);
        }

        response.close();
    }

    private void validateItHasNoAirports() {
        WebTarget path = collect.path("/airports");
        Response response = path.request().get();
        assertThat("Checking the response status to be OK",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));

        assertThat("Validate there are no airports defined.",
            ((Set<String>) response.readEntity(Set.class)).size(), is(0));
    }

    private void checkAirportInformationIsCorrect(AirportData expected) {
        WebTarget path = collect.path("/airport/" + expected.getIata());
        Response response = path.request().get();
        assertThat("Checking the response status to be OK",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));

        AirportData airport = (AirportData) response.readEntity(AirportData.class);
        assertThat("The airport has the right iata.",
            airport.getIata(), is(expected.getIata()));
        assertThat("The airport has the right latitude.",
            airport.getLatitude(), is(expected.getLatitude()));
        assertThat("The airport has the right longitude.",
            airport.getLongitude(), is(expected.getLongitude()));

        response.close();
    }

}
