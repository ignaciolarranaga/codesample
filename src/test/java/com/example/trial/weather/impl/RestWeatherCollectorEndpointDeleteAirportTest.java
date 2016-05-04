package com.example.trial.weather.impl;

import com.example.trial.weather.impl.RestWeatherCollectorEndpoint;
import com.example.trial.weather.WeatherCollectorEndpoint;
import com.example.trial.weather.repository.Repository;
import com.example.trial.weather.repository.RepositoryFactory;
import java.util.Set;
import javax.ws.rs.core.Response;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 * This class contains several test cases for the specific deleteAirport method
 * of RestWeatherCollectorEndpoint.
 * @see RestWeatherCollectorEndpoint
 * @author ignaciolarranaga@gmail.com
 */
public class RestWeatherCollectorEndpointDeleteAirportTest {

    @Before
    public void init() {
        Repository repository = RepositoryFactory.getInstance();
        repository.reset();
    }

    @Test
    public void testDeleteInexistingAirportToBeNotFound() {
        WeatherCollectorEndpoint endpoint = new RestWeatherCollectorEndpoint();

        Response response = endpoint.deleteAirport("BOS");
        assertThat("Checking the response status to be NOT_FOUND.",
            response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void testDeleteWithInvalidIataCodeToBeBadRequest() {
        WeatherCollectorEndpoint endpoint = new RestWeatherCollectorEndpoint();

        Response response = endpoint.deleteAirport("");
        assertThat("Checking the response status to be BAD_REQUEST.",
            response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void testDeleteWithNullIataCodeToBeBadRequest() {
        WeatherCollectorEndpoint endpoint = new RestWeatherCollectorEndpoint();

        Response response = endpoint.deleteAirport(null);
        assertThat("Checking the response status to be BAD_REQUEST.",
            response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void testDeleteWithLowerCaseToBeBadRequest() {
        WeatherCollectorEndpoint endpoint = new RestWeatherCollectorEndpoint();

        Response response = endpoint.deleteAirport("bos");
        assertThat("Checking the response status to be BAD_REQUEST.",
            response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void testNormalFlow() {
        WeatherCollectorEndpoint endpoint = new RestWeatherCollectorEndpoint();

        // Adding one
        Response response = endpoint.addAirport("BOS", "42.364347", "-71.005181");
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        response = endpoint.getAirports();
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat("Contains the added airport.",
            (Set<String>) response.getEntity(),
            containsInAnyOrder("BOS"));

        // Removing it
        response = endpoint.deleteAirport("BOS");
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        response = endpoint.getAirports();
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat("Does not contains the removed airport but the other.",
            (Set<String>) response.getEntity(), is(empty()));
    }
    
    @Test
    public void testThatTheCorrectOneIsDeletedOver2Options() {
        WeatherCollectorEndpoint endpoint = new RestWeatherCollectorEndpoint();

        // Adding one
        Response response = endpoint.addAirport("BOS", "42.364347", "-71.005181");
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        response = endpoint.getAirports();
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat("Contains the added airport.",
            (Set<String>) response.getEntity(),
            containsInAnyOrder("BOS"));

        // Adding the a second one
        endpoint.addAirport("EWR", "40.6925", "-74.168667");
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        response = endpoint.getAirports();
        assertThat("Checking the response status to be OK",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat("Contains both added airports.",
            (Set<String>) response.getEntity(),
            containsInAnyOrder("BOS", "EWR"));

        // Removing the first one
        response = endpoint.deleteAirport("BOS");
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        response = endpoint.getAirports();
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat("Does not contains the removed airport but the other.",
            (Set<String>) response.getEntity(),
            containsInAnyOrder("EWR"));
    }

}
