package com.example.trial.weather.impl;

import com.example.trial.weather.impl.RestWeatherCollectorEndpoint;
import com.example.trial.weather.domain.DataPoint;
import com.example.trial.weather.WeatherCollectorEndpoint;
import com.example.trial.weather.WeatherQueryEndpoint;
import com.example.trial.weather.repository.Repository;
import com.example.trial.weather.repository.RepositoryFactory;
import com.example.trial.weather.test.util.TestUtilities;
import com.google.gson.Gson;
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
public class RestWeatherCollectorEndpointUpdateWeatherTest {

    private final Gson gson = new Gson();

    @Before
    public void init() {
        Repository repository = RepositoryFactory.getInstance();
        repository.reset();
    }

    /**
     * There were typo errors on the service constants
     * This test validates the compatibility was not lost due the fixes
     */
    @Test
    public void testTypoErrorsFixingNotLosingCompatibility() {
        WeatherCollectorEndpoint collect = new RestWeatherCollectorEndpoint();

        collect.addAirport("BOS", "42.364347", "-71.005181");

        // Registering a second measurement
        DataPoint humidityDataPoint = TestUtilities.HUMIDITY_SAMPLE_DATA_POINT;
        Response response = collect.updateWeather("BOS", "humidty",
            gson.toJson(humidityDataPoint));
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void testInexistentAirportToReturnNotFound() {
        WeatherCollectorEndpoint collect = new RestWeatherCollectorEndpoint();

        // Registering a second measurement
        DataPoint humidityDataPoint = TestUtilities.HUMIDITY_SAMPLE_DATA_POINT;
        Response response = collect.updateWeather("BOS", "humidty",
            gson.toJson(humidityDataPoint));
        assertThat("Checking the response status to be NOT_FOUND.",
            response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void testIncorrectIATAToReturnBadRequest() {
        WeatherCollectorEndpoint collect = new RestWeatherCollectorEndpoint();

        // Registering a second measurement
        DataPoint humidityDataPoint = TestUtilities.HUMIDITY_SAMPLE_DATA_POINT;
        Response response = collect.updateWeather("BS", "humidty",
            gson.toJson(humidityDataPoint));
        assertThat("Checking the response status to be BAD_REQUEST.",
            response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    /**
     * This test validates when asking to update an inexistent data point type
     */
    @Test
    public void testInexistentDataPointType() {
        WeatherCollectorEndpoint collect = new RestWeatherCollectorEndpoint();

        // Adding the airport
        Response response = collect.addAirport("BOS", "42.364347", "-71.005181");
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));

        // Registering a second measurement
        DataPoint humidityDataPoint = new DataPoint(5 /* first */,
            15 /* second */, 25 /* third */, 21 /* mean */, 1 /* count */);
        response = collect.updateWeather("BOS", "inexisting",
            gson.toJson(humidityDataPoint));
        assertThat("Checking the response status to be NOT_FOUND.",
            response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    /**
     * This test invalid data point values
     */
    @Test
    public void testInvalidDataPointType() {
        WeatherCollectorEndpoint collect = new RestWeatherCollectorEndpoint();

        // Adding the airport
        Response response = collect.addAirport("BOS", "42.364347", "-71.005181");
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));

        // Registering a second measurement
        DataPoint windDataPoint = new DataPoint(10, 20, 30, -1, 6);
        response = collect.updateWeather("BOS", "wind",
            gson.toJson(windDataPoint));
        assertThat("Checking the response status to be BAD_REQUEST.",
            response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    /**
     * This method tests that the updateWeather operation performs correctly.
     * Due it relations with WeatherQueryEndpoint#weather(String, String) for
     * a complete test both methods have to be used.
     * Many scenarios were included on the query validation, please refer to
     * RestWeatherQueryEndpointWeatherTest for additional tests
     * @see WeatherQueryEndpoint#weather(String, String)
     * @see RestWeatherQueryEndpointWeatherTest
     */
    @Test
    public void testUpdateWeather() {
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

        // Updating one data point
        DataPoint dp = new DataPoint(0, 6, 10, 4, 20);
        response = endpoint.updateWeather("BOS", "wind", gson.toJson(dp));
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }

}
