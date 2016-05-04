package com.example.trial.weather.impl;

import com.example.trial.weather.impl.RestWeatherCollectorEndpoint;
import com.example.trial.weather.WeatherCollectorEndpoint;
import com.example.trial.weather.repository.Repository;
import com.example.trial.weather.repository.RepositoryFactory;
import java.util.Set;
import javax.ws.rs.core.Response;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 * @author ignaciolarranaga@gmail.com
 */
public class RestWeatherCollectorEndpointAddAirportTest {

    @Before
    public void init() {
        Repository repository = RepositoryFactory.getInstance();
        repository.reset();
    }

    @Test
    public void testAddAirportNormalFlow() {
        WeatherCollectorEndpoint endpoint = new RestWeatherCollectorEndpoint();

        // Adding one
        Response response = endpoint.addAirport("BOS", "42.364347", "-71.005181");
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        // Listing the airports for validation
        response = endpoint.getAirports();
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat("Contains the added airport.",
            (Set<String>) response.getEntity(),
            containsInAnyOrder("BOS"));
    }

    @Test
    public void testAddMoreThanOneAirport() {
        WeatherCollectorEndpoint endpoint = new RestWeatherCollectorEndpoint();

        // Adding one
        Response response = endpoint.addAirport("BOS", "42.364347", "-71.005181");
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        // Listing the airports for validation
        response = endpoint.getAirports();
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat("Contains the added airport.",
            (Set<String>) response.getEntity(),
            containsInAnyOrder("BOS"));

        // Adding the a second one
        response = endpoint.addAirport("EWR", "40.6925", "-74.168667");
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        // Listing the airports for validation
        response = endpoint.getAirports();
        assertThat("Checking the response status to be OK",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat("Contains both added airports.",
            (Set<String>) response.getEntity(),
            containsInAnyOrder("BOS", "EWR"));
    }

    @Test
    public void testAirportsNotToBeDuplicated() {
        WeatherCollectorEndpoint endpoint = new RestWeatherCollectorEndpoint();

        // Adding one
        Response response = endpoint.addAirport("BOS", "42.364347", "-71.005181");
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        // Listing the airports for validation
        response = endpoint.getAirports();
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat("Contains the added airport.",
            (Set<String>) response.getEntity(),
            containsInAnyOrder("BOS"));

        // Adding the same again
        response = endpoint.addAirport("BOS", "42.364347", "-71.005181");
        assertThat("Checking the response status to be CONFLICT.",
            response.getStatus(), is(Response.Status.CONFLICT.getStatusCode()));
    }

    @Test
    public void testAddingADuplicateWithDifferentData() {
        WeatherCollectorEndpoint endpoint = new RestWeatherCollectorEndpoint();

        // Adding one
        Response response = endpoint.addAirport("BOS", "42.364347", "-71.005181");
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        // Listing the airports for validation
        response = endpoint.getAirports();
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat("Contains the added airport.",
            (Set<String>) response.getEntity(),
            containsInAnyOrder("BOS"));

        // Adding the second one
        response = endpoint.addAirport("BOS", "-42.364347", "71.005181");
        assertThat("Checking the response status to be CONFLICT.",
            response.getStatus(), is(Response.Status.CONFLICT.getStatusCode()));
    }

    @Test
    public void testAddAirportWithoutIata() {
        WeatherCollectorEndpoint endpoint = new RestWeatherCollectorEndpoint();

        Response response = endpoint.addAirport(null, "42.364347", "-71.005181");
        assertThat("Checking the response status to be BAD_REQUEST.",
            response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void testAddingAnAirportWithWrongIataCode() {
        WeatherCollectorEndpoint endpoint = new RestWeatherCollectorEndpoint();

        // Iata codes should be 3 letters
        Response response = endpoint.addAirport("BO", "-42.364347", "71.005181");
        assertThat("Checking the response status to be BAD_REQUEST.",
            response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));

        response = endpoint.addAirport("", "-42.364347", "71.005181");
        assertThat("Checking the response status to be BAD_REQUEST.",
            response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void testAddingAnAirportWithWrongCoordinates() {
        WeatherCollectorEndpoint endpoint = new RestWeatherCollectorEndpoint();

        // Coordinates are in the range [-90,90] & [-180,180]
        Response response = endpoint.addAirport("BOS", "-91", "71.005181");
        assertThat("Checking the response status to be BAD_REQUEST.",
            response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));

        response = endpoint.addAirport("BOS", "-90.1", "71.005181");
        assertThat("Checking the response status to be BAD_REQUEST.",
            response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));

        response = endpoint.addAirport("BOS", "100", "71.005181");
        assertThat("Checking the response status to be BAD_REQUEST.",
            response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));

        response = endpoint.addAirport("BOS", "-42.364347", "180.005181");
        assertThat("Checking the response status to be BAD_REQUEST.",
            response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));

        response = endpoint.addAirport("BOS", "-42.364347", "-180.005181");
        assertThat("Checking the response status to be BAD_REQUEST.",
            response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));

        // Testing edge case
        response = endpoint.addAirport("BOS", "-90", "180");
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }

}
