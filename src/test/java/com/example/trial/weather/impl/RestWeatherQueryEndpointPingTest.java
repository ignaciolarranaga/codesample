package com.example.trial.weather.impl;

import com.example.trial.weather.impl.RestWeatherCollectorEndpoint;
import com.example.trial.weather.impl.RestWeatherQueryEndpoint;
import com.example.trial.weather.domain.DataPoint;
import com.example.trial.weather.WeatherCollectorEndpoint;
import com.example.trial.weather.WeatherQueryEndpoint;
import com.example.trial.weather.repository.Repository;
import com.example.trial.weather.repository.RepositoryFactory;
import com.example.trial.weather.test.util.TestUtilities;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 * @author ignaciolarranaga@gmail.com
 */
public class RestWeatherQueryEndpointPingTest {

    private final Gson gson = new Gson();

    @Before
    public void init() {
        Repository repository = RepositoryFactory.getInstance();
        repository.reset();
    }

    /**
     * This method validates to perform a ping when no actual call is yet done
     */
    @Test
    public void testPingWithoutAirports() {
        WeatherQueryEndpoint query = new RestWeatherQueryEndpoint();

        // Performing the ping
        String responseString = query.ping();

        // Validating the result
        JsonElement response = new JsonParser().parse(responseString);
        // There should not be data
        assertThat("Validating there is no measurement registered",
            response.getAsJsonObject().get("datasize").getAsInt(), is(0));

        JsonObject iataFreq = response.getAsJsonObject()
            .get("iata_freq").getAsJsonObject();
        assertThat("Validating there are no frequencies as there are no airports",
            iataFreq.entrySet().size(), is(0));

        JsonArray radiusFreq = (JsonArray) response.getAsJsonObject()
            .get("radius_freq");
        assertThat("Validating there is no radious data as there were no calls",
            radiusFreq.size(), is(0));
    }

    /**
     * This method validates to perform a ping when no actual call is yet done
     */
    @Test
    public void testPingWithAirportsWithoutData() {
        WeatherCollectorEndpoint collect = new RestWeatherCollectorEndpoint();
        WeatherQueryEndpoint query = new RestWeatherQueryEndpoint();

        // Adding an airport
        collect.addAirport("BOS", "42.364347", "-71.005181");

        // Performing the ping
        String responseString = query.ping();

        // Validating the result
        JsonElement response = new JsonParser().parse(responseString);
        // There should not be data
        assertThat("Validating there is no measurement registered",
            response.getAsJsonObject().get("datasize").getAsInt(), is(0));

        JsonObject iataFreq = response.getAsJsonObject()
            .get("iata_freq").getAsJsonObject();
        Map<String,Double> expectedFrequencies = new HashMap<>();
        expectedFrequencies.put("BOS", 0.0);
        TestUtilities.validateFrequencies(expectedFrequencies, iataFreq);

        JsonArray radiusFreq = (JsonArray) response.getAsJsonObject()
            .get("radius_freq");
        assertThat("Validating there is no radious data as there were no calls",
            radiusFreq.size(), is(0));
    }

    /**
     * This test validates an entire sample ping
     */
    @Test
    public void testSamplePing() {
        WeatherCollectorEndpoint collect = new RestWeatherCollectorEndpoint();
        WeatherQueryEndpoint query = new RestWeatherQueryEndpoint();

        // Setting 5 Airports
        collect.addAirport("BOS", "42.364347", "-71.005181");
        collect.addAirport("EWR", "40.6925", "-74.168667");
        collect.addAirport("JFK", "40.639751", "-73.778925");
        collect.addAirport("LGA", "40.777245", "-73.872608");
        collect.addAirport("MMU", "40.79935", "-74.4148747");

        // Registering a measurement
        DataPoint sampleDataPoint = new DataPoint(10 /* first */,
            22 /* second */, 30 /* third */, 20 /* mean */, 10 /* count */);
        collect.updateWeather("BOS", "wind", gson.toJson(sampleDataPoint));

        // Performing a query
        query.weather("BOS", "0");

        // Performing the ping
        String response = query.ping();

        // Validating the result
        JsonElement pingResult = new JsonParser().parse(response);
        assertThat("Validating there is only one measurement registered",
            pingResult.getAsJsonObject().get("datasize").getAsInt(), is(1));

        JsonObject iataFreq = pingResult.getAsJsonObject()
            .get("iata_freq").getAsJsonObject();
        Map<String,Double> expectedFrequencies = new HashMap<>();
        expectedFrequencies.put("BOS", 1.0);
        expectedFrequencies.put("EWR", 0.0);
        expectedFrequencies.put("JFK", 0.0);
        expectedFrequencies.put("LGA", 0.0);
        expectedFrequencies.put("MMU", 0.0);
        TestUtilities.validateFrequencies(expectedFrequencies, iataFreq);

        JsonArray radiusFreq = (JsonArray) pingResult.getAsJsonObject()
            .get("radius_freq");
        TestUtilities.validateRadius(new int[]{ 1 }, radiusFreq);
    }

    /**
     * This test validates different airport frequency scenarios
     */
    @Test
    public void testFrequencyCalculation() {
        WeatherCollectorEndpoint collect = new RestWeatherCollectorEndpoint();
        WeatherQueryEndpoint query = new RestWeatherQueryEndpoint();

        // Setting 5 Airports
        collect.addAirport("BOS", "42.364347", "-71.005181");
        collect.addAirport("EWR", "40.6925", "-74.168667");
        collect.addAirport("JFK", "40.639751", "-73.778925");

        // Registering a measurement
        DataPoint sampleDataPoint = new DataPoint(10 /* first */,
            22 /* second */, 30 /* third */, 20 /* mean */, 10 /* count */);
        collect.updateWeather("BOS", "wind", gson.toJson(sampleDataPoint));

        // Performing a query
        query.weather("BOS", "0");

        // Validating the result
        JsonElement result = new JsonParser().parse(query.ping());
        JsonObject iataFreq = result.getAsJsonObject()
            .get("iata_freq").getAsJsonObject();
        Map<String,Double> expectedFrequencies = new HashMap<>();
        expectedFrequencies.put("BOS", 1.0);
        expectedFrequencies.put("EWR", 0.0);
        expectedFrequencies.put("JFK", 0.0);
        TestUtilities.validateFrequencies(expectedFrequencies, iataFreq);

        // Performing a second query
        query.weather("BOS", "0");

        // Validating the result
        result = new JsonParser().parse(query.ping());
        iataFreq = result.getAsJsonObject().get("iata_freq").getAsJsonObject();
        // It should be still the same as the new call was on the same IATA
        TestUtilities.validateFrequencies(expectedFrequencies, iataFreq);

        // Performing 2 queries on a different airport
        query.weather("JFK", "0");
        query.weather("JFK", "0");

        // Validating the result
        result = new JsonParser().parse(query.ping());
        iataFreq = result.getAsJsonObject().get("iata_freq").getAsJsonObject();
        // It should be 50% / 50% for each airport
        expectedFrequencies.put("BOS", 0.5);
        expectedFrequencies.put("EWR", 0.0);
        expectedFrequencies.put("JFK", 0.5);
        TestUtilities.validateFrequencies(expectedFrequencies, iataFreq);
    }

    /**
     * This test validates different radius histogram scenarios
     */
    @Test
    public void testRadiusHistogram() {
        WeatherCollectorEndpoint collect = new RestWeatherCollectorEndpoint();
        WeatherQueryEndpoint query = new RestWeatherQueryEndpoint();

        // Setting 5 Airports
        collect.addAirport("BOS", "42.364347", "-71.005181");
        collect.addAirport("EWR", "40.6925", "-74.168667");

        // Registering a measurement
        DataPoint sampleDataPoint = new DataPoint(10 /* first */,
            22 /* second */, 30 /* third */, 20 /* mean */, 10 /* count */);
        collect.updateWeather("BOS", "wind", gson.toJson(sampleDataPoint));

        // Performing a query
        query.weather("BOS", "0");

        // Validating the result
        JsonElement result = new JsonParser().parse(query.ping());
        JsonArray radiusFreq = (JsonArray) result.getAsJsonObject()
            .get("radius_freq");
        TestUtilities.validateRadius(new int[]{ 1 }, radiusFreq);

        // Performing a second query on the same radius
        query.weather("BOS", "0");

        // Validating the result
        result = new JsonParser().parse(query.ping());
        radiusFreq = (JsonArray) result.getAsJsonObject()
            .get("radius_freq");
        TestUtilities.validateRadius(new int[]{ 2 }, radiusFreq);

        // Performing a third query on 5 km for the same airport
        query.weather("BOS", "5");

        // Validating the result
        result = new JsonParser().parse(query.ping());
        radiusFreq = (JsonArray) result.getAsJsonObject()
            .get("radius_freq");
        TestUtilities.validateRadius(new int[]{ 2, 0, 0, 0, 0, 1 }, radiusFreq);

        // Performing a third query on 5 km for a different airport
        query.weather("EWR", "5");

        // Validating the result
        result = new JsonParser().parse(query.ping());
        radiusFreq = (JsonArray) result.getAsJsonObject()
            .get("radius_freq");
        TestUtilities.validateRadius(new int[]{ 2, 0, 0, 0, 0, 2 }, radiusFreq);

        // Performing a fourth query on 2000 km (it should be count on 1000)
        query.weather("EWR", "2000");

        // Validating the result
        result = new JsonParser().parse(query.ping());
        radiusFreq = (JsonArray) result.getAsJsonObject()
            .get("radius_freq");
        int[] expectedResult = new int[1001];
        expectedResult[0] = 2;
        expectedResult[5] = 2;
        expectedResult[1000] = 1;
        TestUtilities.validateRadius(expectedResult, radiusFreq);

        // Performing a fifth query on 3000 km (it should be count on 1000)
        query.weather("EWR", "3000");

        // Validating the result
        result = new JsonParser().parse(query.ping());
        radiusFreq = (JsonArray) result.getAsJsonObject()
            .get("radius_freq");
        expectedResult[1000] = 2;
        TestUtilities.validateRadius(expectedResult, radiusFreq);
    }

}
