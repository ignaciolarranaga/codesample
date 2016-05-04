package com.example.trial.weather.impl;

import com.example.trial.weather.impl.RestWeatherCollectorEndpoint;
import com.example.trial.weather.impl.RestWeatherQueryEndpoint;
import com.example.trial.weather.WeatherCollectorEndpoint;
import com.example.trial.weather.WeatherQueryEndpoint;
import com.example.trial.weather.domain.AtmosphericInformation;
import com.example.trial.weather.domain.DataPoint;
import com.example.trial.weather.repository.Repository;
import com.example.trial.weather.repository.RepositoryFactory;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 * Legacy test cases. They almost completely replaced by newer and powerful
 * versions.
 * It was only adjusted in formatting
 * @author code test administrator
 */
@Deprecated
public class WeatherEndpointTest {

    /**
     * The query endpoint used for testing
     */
    private WeatherQueryEndpoint query = new RestWeatherQueryEndpoint();

    /**
     * The collect endpoint used for testing
     */
    private WeatherCollectorEndpoint collect = new RestWeatherCollectorEndpoint();

    /**
     * The Gson instance used in the test
     */
    private Gson gson = new Gson();

    /**
     * One shared data point used in the tests
     */
    private DataPoint dataPoint;
	
    @Before
    public void setUp() throws Exception {
        Repository repository = RepositoryFactory.getInstance();
        repository.reset();

        // Initializing the airports
        collect.addAirport("BOS", "42.364347", "-71.005181");
        collect.addAirport("EWR", "40.6925", "-74.168667");
        collect.addAirport("JFK", "40.639751", "-73.778925");
        collect.addAirport("LGA", "40.777245", "-73.872608");
        collect.addAirport("MMU", "40.79935", "-74.4148747");

        // Creating a data point
        dataPoint = new DataPoint(10 /* first */,
            22 /* second */, 30 /* third */, 20 /* mean */, 10 /* count */);

        // Initializing a data point
        collect.updateWeather("BOS", "wind", gson.toJson(dataPoint));

        // Performing a
        query.weather("BOS", "0").getEntity();
    }

    /**
     * @deprecated Already replaced by RestWeatherQueryEndpointPingTest
     * @see RestWeatherQueryEndpointPingTest
     */
    @Test
    public void testPing() throws Exception {
        String ping = query.ping();
        JsonElement pingResult = new JsonParser().parse(ping);
        assertEquals(1, pingResult.getAsJsonObject().get("datasize").getAsInt());
        assertEquals(5, pingResult.getAsJsonObject().get("iata_freq")
            .getAsJsonObject().entrySet().size());
    }

    /**
     * @deprecated Already replaced by RestWeatherQueryEndpointWeatherTest
     * @see RestWeatherQueryEndpointWeatherTest
     */
    @Test
    public void testGet() throws Exception {
        List<AtmosphericInformation> ais = (List<AtmosphericInformation>)
            query.weather("BOS", "0").getEntity();
        assertEquals(ais.get(0).getWind(), dataPoint);
    }

    //TODO: Replace/improve this test
    @Test
    public void testGetNearby() throws Exception {
        // check datasize response
        collect.updateWeather("JFK", "wind", gson.toJson(dataPoint));
        dataPoint.setMean(40);
        collect.updateWeather("EWR", "wind", gson.toJson(dataPoint));
        dataPoint.setMean(30);
        collect.updateWeather("LGA", "wind", gson.toJson(dataPoint));

        List<AtmosphericInformation> ais = (List<AtmosphericInformation>)
                query.weather("JFK", "200").getEntity();

        assertEquals(3, ais.size());
    }

    /**
     * @deprecated Already replaced by RestWeatherCollectorEndpointUpdateWeatherTest
     * @see RestWeatherCollectorEndpointUpdateWeatherTest
     */
    @Test
    public void testUpdate() throws Exception {
        DataPoint windDp = new DataPoint(10 /* first */, 22 /* second */,
            30 /* third */, 20 /* mean */, 10 /* count */);
        collect.updateWeather("BOS", "wind", gson.toJson(windDp));
        query.weather("BOS", "0").getEntity();

        String ping = query.ping();
        JsonElement pingResult = new JsonParser().parse(ping);
        assertEquals(1, pingResult.getAsJsonObject().get("datasize").getAsInt());

        DataPoint cloudCoverDp = new DataPoint(10 /* first */, 50 /* second */,
            100 /* third */, 60 /* mean */, 4 /* count */);
        collect.updateWeather("BOS", "cloudcover", gson.toJson(cloudCoverDp));

        List<AtmosphericInformation> ais = (List<AtmosphericInformation>)
            query.weather("BOS", "0").getEntity();
        assertEquals(ais.get(0).getWind(), windDp);
        assertEquals(ais.get(0).getCloudCover(), cloudCoverDp);
    }

}