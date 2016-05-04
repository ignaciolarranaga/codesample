package com.example.trial.weather.impl;

import com.example.trial.weather.impl.RestWeatherCollectorEndpoint;
import com.example.trial.weather.impl.RestWeatherQueryEndpoint;
import com.example.trial.weather.domain.AirportData;
import com.example.trial.weather.domain.AtmosphericInformation;
import com.example.trial.weather.domain.DataPoint;
import com.example.trial.weather.repository.Repository;
import com.example.trial.weather.repository.RepositoryFactory;
import com.example.trial.weather.test.util.TestUtilities;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import org.junit.AfterClass;
import static org.junit.Assert.assertThat;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author ignaciolarranaga@gmail.com
 */
public class RestWeatherIntegrationTest {

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
     * This test case walks through a sample scenario touching every single
     * endpoint method.
     * This test does not intend to test functionality but integration.
     * Functionality was tested on separated unit tests
     */
    @Test
    public void testSampleScenario() {
        checkCollectPing();
        checkNoAirportsDefined();
        addTheSampleAirport();
        checkTheSampleAirportIsDefined();
        checkTheSampleAirportDataStored();

        checkQueryPing();
        checkThereIsYetNoWeatherInformationForTheSampleAirport();
        updateSampleWeatherInformation();
        checkSamepleWeatherInformationCorrectlyReceived();
        deleteTheSampleAirport();
        checkNoAirportsDefined();

        // Exiting
        // collect.path("/exit");
        // This can not be tested, as suggested it should not exist event
    }

    private void deleteTheSampleAirport() {
        WebTarget path = collect.path("/airport/BOS");
        Response response = path.request().delete();
        
        assertThat("Checking the response status to be OK",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));

        response.close();
    }

    private void checkSamepleWeatherInformationCorrectlyReceived() {
        // Making the call
        WebTarget path = query.path("/weather/BOS/0");
        Response response = path.request().get();
        assertThat("Checking the response status to be OK",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        List<AtmosphericInformation> resultList = response.readEntity(
            new GenericType<List<AtmosphericInformation>>(){});

        // Preparing the expected
        AtmosphericInformation expected = new AtmosphericInformation();
        expected.setCloudCover(new DataPoint(10 /* first */, 50 /* second */,
            100 /* third */, 60 /* mean */, 4 /* count */));
        TestUtilities.adjustLastUpdateTime(expected, resultList);

        assertThat("Contains the expected data point.",
            resultList, contains(expected));

        response.close();
    }

    private void updateSampleWeatherInformation() {
        WebTarget path = collect.path("/weather/BOS/cloudcover");
        DataPoint dataPoint = new DataPoint(10 /* first */, 50 /* second */,
            100 /* third */, 60 /* mean */, 4 /* count */);
        Response response = path.request().post(Entity.entity(dataPoint,
            MediaType.APPLICATION_JSON));

        assertThat("Checking the response status to be OK",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));

        response.close();
    }

    private void checkThereIsYetNoWeatherInformationForTheSampleAirport() {
        WebTarget path = query.path("/weather/BOS/0");
        Response response = path.request().get();
        assertThat("Checking the response status to be OK",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));

        // Preparing the expected
        List<AtmosphericInformation> resultList = response.readEntity(
            new GenericType<List<AtmosphericInformation>>(){});
        AtmosphericInformation expected = new AtmosphericInformation();

        assertThat("Contains the expected data point (an empty one).",
            resultList, contains(expected));

        response.close();
    }

    private void checkQueryPing() throws JsonSyntaxException {
        WebTarget path = query.path("/ping");
        Response response = path.request().get();
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));

        // Valindating the datasize
        JsonElement pingResult = new JsonParser().parse(response.readEntity(String.class));
        assertThat("Validating there are no measurements registered",
            pingResult.getAsJsonObject().get("datasize").getAsInt(), is(0));

        // Validating the IATA frequencies
        JsonObject iataFreq = pingResult.getAsJsonObject()
            .get("iata_freq").getAsJsonObject();
        Map<String,Double> expectedFrequencies = new HashMap<>();
        expectedFrequencies.put("BOS", 0.0);
        TestUtilities.validateFrequencies(expectedFrequencies, iataFreq);

        // Validating the radius frequencies
        JsonArray radiusFreq = (JsonArray) pingResult.getAsJsonObject()
                .get("radius_freq");
        TestUtilities.validateRadius(new int[0], radiusFreq);

        response.close();
    }

    private void checkTheSampleAirportDataStored() {
        WebTarget path = collect.path("/airport/BOS");
        Response response = path.request().get();
        assertThat("Checking the response status to be OK",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));

        AirportData airport = (AirportData) response.readEntity(AirportData.class);
        assertThat("The airport has the right iata.",
            airport.getIata(), is("BOS"));
        assertThat("The airport has the right latitude.",
            airport.getLatitude(), is(42.364347));
        assertThat("The airport has the right longitude.",
            airport.getLongitude(), is(-71.005181));

        response.close();
    }

    private void checkTheSampleAirportIsDefined() {
        WebTarget path = collect.path("/airports");
        Response response = path.request().get();
        assertThat("Checking the response status to be OK",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));

        assertThat("Contains the added airport but not repeated.",
            (Set<String>) response.readEntity(Set.class),
            containsInAnyOrder("BOS"));

        response.close();
    }

    private void addTheSampleAirport() {
        WebTarget path = collect.path("/airport/BOS/42.364347/-71.005181/");
        Response response = path.request().post(null, Response.class);
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));

        response.close();
    }

    private void checkNoAirportsDefined() {
        WebTarget path = collect.path("/airports");
        Response response = path.request().get();
        assertThat("Checking the response status of listing airports to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));

        assertThat("Checking there are no airports.",
            (Set<String>) response.readEntity(Set.class), is(empty()));

        response.close();
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
