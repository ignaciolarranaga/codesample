package com.example.trial.weather.impl;

import com.example.trial.weather.impl.RestWeatherCollectorEndpoint;
import com.example.trial.weather.impl.RestWeatherQueryEndpoint;
import com.example.trial.weather.domain.AtmosphericInformation;
import com.example.trial.weather.domain.DataPoint;
import com.example.trial.weather.WeatherCollectorEndpoint;
import com.example.trial.weather.WeatherQueryEndpoint;
import com.example.trial.weather.repository.Repository;
import com.example.trial.weather.repository.RepositoryFactory;
import com.example.trial.weather.test.util.TestUtilities;
import com.google.gson.Gson;
import java.util.List;
import javax.ws.rs.core.Response;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 * @author ignaciolarranaga@gmail.com
 */
public class RestWeatherQueryEndpointWeatherTest {

    private final Gson gson = new Gson();

    @Before
    public void init() {
        Repository repository = RepositoryFactory.getInstance();
        repository.reset();
    }

    /**
     * This test validates the BAD_REQUEST response with an invalid radius
     */
    @Test
    public void testInvalidRadius() {
        WeatherCollectorEndpoint collect = new RestWeatherCollectorEndpoint();
        WeatherQueryEndpoint query = new RestWeatherQueryEndpoint();

        // Setting the Airports
        collect.addAirport("BOS", "42.364347", "-71.005181");

        // Registering a measurement
        DataPoint sampleDataPoint = new DataPoint(10 /* first */,
            22 /* second */, 30 /* third */, 20 /* mean */, 10 /* count */);
        collect.updateWeather("BOS", "wind", gson.toJson(sampleDataPoint));

        // Performing a call
        Response response = query.weather("BOS", "Nan");

        // Validating the result
        assertThat("Checking the response status to be BAD_REQUEST.",
            response.getStatus(),
            is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    /**
     * This test validates when asking for results for an inexistent airport
     */
    @Test
    public void testInexistentAirport() {
        WeatherQueryEndpoint query = new RestWeatherQueryEndpoint();

        // Performing a call
        Response response = query.weather("BOS", "0");

        // Validating the result
        assertThat("Checking the response status to be NOT_FOUND.",
            response.getStatus(),
            is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    /**
     * This test validates when asking for results before data to exists
     */
    @Test
    public void testInocationWhenThereIsStillNoData() {
        WeatherCollectorEndpoint collect = new RestWeatherCollectorEndpoint();
        WeatherQueryEndpoint query = new RestWeatherQueryEndpoint();

        // Setting the Airports
        collect.addAirport("BOS", "42.364347", "-71.005181");

        // Performing a call
        Response response = query.weather("BOS", "0");

        // Validating the result
        assertThat("Checking the response status to be OK.",
            response.getStatus(),
            is(Response.Status.OK.getStatusCode()));
        List<AtmosphericInformation> resultList =
            (List<AtmosphericInformation>) response.getEntity();
        AtmosphericInformation expected = new AtmosphericInformation();
        assertThat("Contains the expected data point (an empty one).",
            resultList, contains(expected));
    }

    /**
     * This test validates an entire sample weather request
     */
    @Test
    public void testSampleWeather() {
        WeatherCollectorEndpoint collect = new RestWeatherCollectorEndpoint();

        // Setting 5 Airports
        collect.addAirport("BOS", "42.364347", "-71.005181");
        collect.addAirport("EWR", "40.6925", "-74.168667");
        collect.addAirport("JFK", "40.639751", "-73.778925");
        collect.addAirport("LGA", "40.777245", "-73.872608");
        collect.addAirport("MMU", "40.79935", "-74.4148747");

        // Registering a measurement
        DataPoint sampleDataPoint = new DataPoint(10 /* first */,
            22 /* second */, 30 /* third */, 20 /* mean */, 10 /* count */);
        Response response = collect.updateWeather("BOS", "wind", gson.toJson(sampleDataPoint));
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));

        // Validating the result
        AtmosphericInformation expected = new AtmosphericInformation();
        expected.setWind(sampleDataPoint);
        checkExpectedAtmosphericInformationInPlace("BOS", expected);
    }

    @Test
    public void testEverySingleDataPoint() {
        WeatherCollectorEndpoint collect = new RestWeatherCollectorEndpoint();

        // Setting the airport
        collect.addAirport("BOS", "42.364347", "-71.005181");

        AtmosphericInformation expected = new AtmosphericInformation();

        // Temprerature
        Response response = collect.updateWeather("BOS", "temperature",
            gson.toJson(TestUtilities.TEMPERATURE_SAMPLE_DATA_POINT));
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        expected.setTemperature(TestUtilities.TEMPERATURE_SAMPLE_DATA_POINT);
        checkExpectedAtmosphericInformationInPlace("BOS", expected);

        // Wind
        response = collect.updateWeather("BOS", "wind",
            gson.toJson(TestUtilities.WIND_SAMPLE_DATA_POINT));
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        expected.setWind(TestUtilities.WIND_SAMPLE_DATA_POINT);
        checkExpectedAtmosphericInformationInPlace("BOS", expected);

        // Humidity
        response = collect.updateWeather("BOS", "humidty",
            gson.toJson(TestUtilities.HUMIDITY_SAMPLE_DATA_POINT));
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        expected.setHumidity(TestUtilities.HUMIDITY_SAMPLE_DATA_POINT);
        checkExpectedAtmosphericInformationInPlace("BOS", expected);

        // Precipitation
        response = collect.updateWeather("BOS", "precipitation",
            gson.toJson(TestUtilities.PRECIPITATION_SAMPLE_DATA_POINT));
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        expected.setPrecipitation(TestUtilities.PRECIPITATION_SAMPLE_DATA_POINT);
        checkExpectedAtmosphericInformationInPlace("BOS", expected);

        // Pressure
        response = collect.updateWeather("BOS", "pressure",
            gson.toJson(TestUtilities.PRESSURE_SAMPLE_DATA_POINT));
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        expected.setPressure(TestUtilities.PRESSURE_SAMPLE_DATA_POINT);
        checkExpectedAtmosphericInformationInPlace("BOS", expected);

        // CloudCover
        response = collect.updateWeather("BOS", "cloudcover",
            gson.toJson(TestUtilities.CLOUD_COVER_SAMPLE_DATA_POINT));
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        expected.setCloudCover(TestUtilities.CLOUD_COVER_SAMPLE_DATA_POINT);
        checkExpectedAtmosphericInformationInPlace("BOS", expected);
    }

    /**
     * This test validates several update operations
     */
    @Test
    public void testWeatherUpdate() {
        WeatherCollectorEndpoint collect = new RestWeatherCollectorEndpoint();
        WeatherQueryEndpoint query = new RestWeatherQueryEndpoint();

        // Setting 5 Airports
        collect.addAirport("BOS", "42.364347", "-71.005181");
        collect.addAirport("EWR", "40.6925", "-74.168667");

        // Registering a measurement
        DataPoint firstWindDataPoint = new DataPoint(10 /* first */,
            22 /* second */, 30 /* third */, 20 /* mean */, 10 /* count */);
        collect.updateWeather("BOS", "wind", gson.toJson(firstWindDataPoint));

        // Performing a call & validate the result
        Response response = query.weather("BOS", "0");
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        List<AtmosphericInformation> resultList =
            (List<AtmosphericInformation>) response.getEntity();
        AtmosphericInformation expected = new AtmosphericInformation();
        expected.setWind(firstWindDataPoint);
        TestUtilities.adjustLastUpdateTime(expected, resultList);
        assertThat("Contains the expected data point for.",
            resultList, contains(expected));

        // Registering a second measurement
        DataPoint secondWindDataPoint = new DataPoint(11 /* first */,
            21 /* second */, 31 /* third */, 20 /* mean */, 11 /* count */);
        collect.updateWeather("BOS", "wind", gson.toJson(secondWindDataPoint));

        // Performing a call & validate we get the new result
        response = query.weather("BOS", "0");
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        resultList = (List<AtmosphericInformation>) response.getEntity();
        expected = new AtmosphericInformation();
        expected.setWind(secondWindDataPoint);
        TestUtilities.adjustLastUpdateTime(expected, resultList);
        assertThat("Contains the expected data point (the updated, not the old).",
            resultList, contains(expected));

        // Registering a second measurement
        DataPoint humidityDataPoint = new DataPoint(5 /* first */,
            15 /* second */, 25 /* third */, 21 /* mean */, 1 /* count */);
        collect.updateWeather("BOS", "humidty", gson.toJson(humidityDataPoint));

        // Performing a call & validate we get the combined result
        response = query.weather("BOS", "0");
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        resultList = (List<AtmosphericInformation>) response.getEntity();
        expected.setHumidity(humidityDataPoint);
        TestUtilities.adjustLastUpdateTime(expected, resultList);
        assertThat("Contains the expected data point (combining the 2 dps).",
            resultList, contains(expected));

        // Registering a second measurement
        DataPoint otherAirportDataPoint = new DataPoint(5 /* first */,
            15 /* second */, 25 /* third */, 21 /* mean */, 1 /* count */);
        collect.updateWeather("EWR", "wind", gson.toJson(otherAirportDataPoint));

        // Performing a call & validate we get the same result
        response = query.weather("BOS", "0");
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));
        resultList = (List<AtmosphericInformation>) response.getEntity();
        //expected is not updated
        assertThat("Contains the expected data point (the same that before).",
            resultList, contains(expected));
    }

    /**
     * This method checks the expected atmospheric information for an airport
     * identified by the iata code.
     * @param iata The code of the airport to check
     * @param expected The expected atmospheric information
     */
    private void checkExpectedAtmosphericInformationInPlace(String iata,
        AtmosphericInformation expected) {
        WeatherQueryEndpoint query = new RestWeatherQueryEndpoint();

        // Querying the weather
        Response response = query.weather(iata, "0");
        assertThat("Checking the response status to be OK.",
            response.getStatus(), is(Response.Status.OK.getStatusCode()));

        // Validating the expected data point
        List<AtmosphericInformation> resultList =
            (List<AtmosphericInformation>) response.getEntity();
        TestUtilities.adjustLastUpdateTime(expected, resultList);
        assertThat("Contains the expected airport information.",
            resultList, contains(expected));
    }

}
