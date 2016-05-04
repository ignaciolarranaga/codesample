package com.example.trial.weather.impl;

import com.example.trial.weather.impl.RestWeatherCollectorEndpoint;
import com.example.trial.weather.impl.RestWeatherQueryEndpoint;
import com.example.trial.weather.WeatherCollectorEndpoint;
import com.example.trial.weather.WeatherQueryEndpoint;
import com.example.trial.weather.repository.Repository;
import com.example.trial.weather.repository.RepositoryFactory;
import com.example.trial.weather.test.util.TestUtilities;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 * @author ignaciolarranaga@gmail.com
 */
public class RestWeatherQueryEndpointPingDatasizeTest {
    
    private final Gson gson = new Gson();

    @Before
    public void init() {
        Repository repository = RepositoryFactory.getInstance();
        repository.reset();
    }

    @Test
    public void testCeroDatasize() {
        // Validating the result
        checkCount(0);
    }

    @Test
    public void testDatasizeForOnlyOneAirport() {
        WeatherCollectorEndpoint collect = new RestWeatherCollectorEndpoint();

        // Setting an airport
        collect.addAirport("BOS", "42.364347", "-71.005181");

        // Registering a new data point and checking the data size
        collect.updateWeather("BOS", "wind", gson.toJson(TestUtilities.WIND_SAMPLE_DATA_POINT));
        checkCount(1);

        // Registering a new data point and checking the data size
        collect.updateWeather("BOS", "temperature", gson.toJson(TestUtilities.TEMPERATURE_SAMPLE_DATA_POINT));
        checkCount(2);

        // Registering a new data point and checking the data size
        collect.updateWeather("BOS", "humidty", gson.toJson(TestUtilities.HUMIDITY_SAMPLE_DATA_POINT));
        checkCount(3);

        // Registering a new data point and checking the data size
        collect.updateWeather("BOS", "pressure", gson.toJson(TestUtilities.PRESSURE_SAMPLE_DATA_POINT));
        checkCount(4);

        // Registering a new data point and checking the data size
        collect.updateWeather("BOS", "cloudcover", gson.toJson(TestUtilities.CLOUD_COVER_SAMPLE_DATA_POINT));
        checkCount(5);

        // Registering a new data point and checking the data size
        collect.updateWeather("BOS", "precipitation", gson.toJson(TestUtilities.PRECIPITATION_SAMPLE_DATA_POINT));
        checkCount(6);
    }

    @Test
    public void testDatasizeWhenRepeatingMeasures() {
        WeatherCollectorEndpoint collect = new RestWeatherCollectorEndpoint();

        // Setting an airport
        collect.addAirport("BOS", "42.364347", "-71.005181");

        // Registering a new data point and checking the data size
        collect.updateWeather("BOS", "wind", gson.toJson(TestUtilities.WIND_SAMPLE_DATA_POINT));
        checkCount(1);

        // Registering a new data point and checking the data size
        collect.updateWeather("BOS", "wind", gson.toJson(TestUtilities.WIND_SAMPLE_DATA_POINT));
        checkCount(1);       // It should stay in 1
    }

    @Test
    public void testDatasizeForTwoAirports() {
        WeatherCollectorEndpoint collect = new RestWeatherCollectorEndpoint();
        WeatherQueryEndpoint query = new RestWeatherQueryEndpoint();

        // Setting an airport
        collect.addAirport("BOS", "42.364347", "-71.005181");
        collect.addAirport("EWR", "40.6925", "-74.168667");

        // Registering a new data point and checking the data size
        collect.updateWeather("BOS", "wind", gson.toJson(TestUtilities.WIND_SAMPLE_DATA_POINT));
        checkCount(1);

        // Registering a new data point and checking the data size
        collect.updateWeather("EWR", "wind", gson.toJson(TestUtilities.WIND_SAMPLE_DATA_POINT));
        checkCount(2);

        // Registering a new data point and checking the data size
        collect.updateWeather("BOS", "temperature", gson.toJson(TestUtilities.TEMPERATURE_SAMPLE_DATA_POINT));
        collect.updateWeather("BOS", "humidty", gson.toJson(TestUtilities.HUMIDITY_SAMPLE_DATA_POINT));
        collect.updateWeather("BOS", "pressure", gson.toJson(TestUtilities.PRESSURE_SAMPLE_DATA_POINT));
        collect.updateWeather("BOS", "cloudcover", gson.toJson(TestUtilities.CLOUD_COVER_SAMPLE_DATA_POINT));
        collect.updateWeather("BOS", "precipitation", gson.toJson(TestUtilities.PRECIPITATION_SAMPLE_DATA_POINT));
        checkCount(7);  // 5+ 2
    }

    /**
     * This method checks the number of data points in memory and compare it
     * with the expected value provided.
     * @param expected The expected value
     */
    private void checkCount(int expected) {
        WeatherQueryEndpoint query = new RestWeatherQueryEndpoint();

        String response = query.ping();
        JsonElement pingResult = new JsonParser().parse(response);
        assertThat("Validating the number of measurements is the expected.",
            pingResult.getAsJsonObject().get("datasize").getAsInt(), is(expected));
    }

}
