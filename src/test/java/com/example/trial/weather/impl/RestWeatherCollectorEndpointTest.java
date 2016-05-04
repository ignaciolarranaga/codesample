package com.example.trial.weather.impl;

import com.example.trial.weather.impl.RestWeatherCollectorEndpoint;
import com.example.trial.weather.domain.AirportData;
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
public class RestWeatherCollectorEndpointTest {

    @Before
    public void init() {
        Repository repository = RepositoryFactory.getInstance();
        repository.reset();
    }

    @Test
    public void testPingReturning1() {
        WeatherCollectorEndpoint endpoint = new RestWeatherCollectorEndpoint();
        
        Response response = endpoint.ping();
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat("Checking the expected response: @return 1 if the endpoint "
            + "is alive functioning, 0 otherwise.", response.getEntity(), is(1));
    }

    @Test
    public void testGetAirports() {
        WeatherCollectorEndpoint endpoint = new RestWeatherCollectorEndpoint();

        // Adding some airports to validate
        endpoint.addAirport("BOS", "42.364347", "-71.005181");
        endpoint.addAirport("EWR", "40.6925", "-74.168667");
        endpoint.addAirport("JFK", "40.639751", "-73.778925");
        endpoint.addAirport("LGA", "40.777245", "-73.872608");
        endpoint.addAirport("MMU", "40.79935", "-74.4148747");

        Response response = endpoint.getAirports();
        assertThat("Checking the response status to be OK",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat("Contains the added airport but not repeated.",
            (Set<String>) response.getEntity(),
            containsInAnyOrder("BOS", "EWR", "JFK", "LGA", "MMU"));
    }

}
