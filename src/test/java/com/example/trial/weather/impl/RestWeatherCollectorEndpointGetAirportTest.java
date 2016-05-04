package com.example.trial.weather.impl;

import com.example.trial.weather.impl.RestWeatherCollectorEndpoint;
import com.example.trial.weather.WeatherCollectorEndpoint;
import com.example.trial.weather.domain.AirportData;
import com.example.trial.weather.repository.Repository;
import com.example.trial.weather.repository.RepositoryFactory;
import javax.ws.rs.core.Response;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 * @author ignaciolarranaga@gmail.com
 */
public class RestWeatherCollectorEndpointGetAirportTest {

    @Before
    public void init() {
        Repository repository = RepositoryFactory.getInstance();
        repository.reset();
    }

    @Test
    public void testNormalScenario() {
        WeatherCollectorEndpoint endpoint = new RestWeatherCollectorEndpoint();

        // Adding one
        endpoint.addAirport("BOS", "42.364347", "-71.005181");

        Response response = endpoint.getAirport("BOS");
        assertThat("Checking the response status to be OK",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        AirportData airportData = (AirportData) response.getEntity();
        assertThat("Checking that the airport iata is correct.",
            airportData.getIata(), is("BOS"));
        assertThat("Checking that the airport latitude is correct.",
            airportData.getLatitude(), is(42.364347));
        assertThat("Checking that the airport latitude is correct.",
            airportData.getLongitude(), is(-71.005181));
    }

    @Test
    public void testInexistentAirportToReturnNotFound() {
        WeatherCollectorEndpoint endpoint = new RestWeatherCollectorEndpoint();

        Response response = endpoint.getAirport("BOS");
        assertThat("Checking the response status to be NOT_FOUND",
            response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void testNullIATAToReturnBadRequest() {
        WeatherCollectorEndpoint endpoint = new RestWeatherCollectorEndpoint();

        Response response = endpoint.getAirport(null);
        assertThat("Checking the response status to be BAD_REQUEST",
            response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void testIncorrectIATAToReturnBadRequest() {
        WeatherCollectorEndpoint endpoint = new RestWeatherCollectorEndpoint();

        Response response = endpoint.getAirport("BS");
        assertThat("Checking the response status to be BAD_REQUEST",
            response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void testLowerCaseIATAToReturnBadRequest() {
        WeatherCollectorEndpoint endpoint = new RestWeatherCollectorEndpoint();

        Response response = endpoint.getAirport("bos");
        assertThat("Checking the response status to be BAD_REQUEST",
            response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

}
